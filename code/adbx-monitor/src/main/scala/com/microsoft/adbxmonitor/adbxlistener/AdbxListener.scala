package com.microsoft.adbxmonitor.adbxlistener

import org.apache.spark
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.scheduler._
import java.util._
import scala.collection.JavaConverters._
import com.databricks.dbutils_v1.DBUtilsHolder.dbutils
import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.TelemetryConfiguration

class AdbxListener extends SparkListener  {

  val configuration = com.microsoft.applicationinsights.TelemetryConfiguration.createDefault()
  configuration.setInstrumentationKey(System.getenv("APPINSIGHTS_INSTRUMENTATIONKEY"))
  val telemetryClient = new TelemetryClient(configuration)

  onListenerRegister()    // call our fake event to log the instantiation of this class

  // returns the Config object for Databricks
  private def getDatabricksInfo():com.databricks.backend.daemon.driver.DriverConf = {
    import com.databricks.backend.common.util.Project
    import com.databricks.conf.trusted.ProjectConf
    import com.databricks.backend.daemon.driver.DriverConf

    new DriverConf(ProjectConf.loadLocalConfig(Project.Driver))
  }

  // safely gets a value from the Databricks Cluster Usage Tag collection
  private def getClusterUsageTag(dbrInfo: com.databricks.backend.daemon.driver.DriverConf, key: String): Option[String] = {
      try {
          Some(dbrInfo.clusterUsageTags(key))
      } catch {
          case e: NoSuchElementException => None
      }
  }
  
  // safely gets a value from the Spark config
  private def getSparkConfValue(sparkSession: Option[SparkSession], key: String): Option[String] = {
    try {
        Some(sparkSession.get.conf.get(key))
    } catch {
        case e: NoSuchElementException => None
    }
  }

  // gather all of the values we want to send to AppInsights
  private def buildProperties() : HashMap[String, String] = {
    val properties = collection.mutable.Map[String, Option[String]]()

    // get some properties off of the Databricks DriverConf object
    val dbrInfo = getDatabricksInfo()
    properties.put("region", Some(dbrInfo.region))
    properties.put("driverNodeTypeId", dbrInfo.driverNodeTypeId)
    properties.put("workerNodeTypeId", dbrInfo.workerNodeTypeId)
    
    // get tag values from the Databricks DriverConf object
    val tagKeys = scala.List("clusterAllTags", "sparkVersion", "clusterId", "clusterName")
    val tagValues = tagKeys.map(k => k -> { getClusterUsageTag(dbrInfo, k) }).toMap
    properties ++= tagValues

    // get values from the spark conf... including some custom values that we may stashed in there ourselves
    val sparkKeys = scala.collection.mutable.Map(
      "appId" -> "spark.app.id",
      "clusterScalingType" -> "spark.databricks.clusterUsageTags.clusterScalingType",
      "clusterTargetWorkers" -> "spark.databricks.clusterUsageTags.clusterTargetWorkers",
      "clusterWorkers" -> "spark.databricks.clusterUsageTags.clusterWorkers",
      "workspaceId" -> "spark.databricks.clusterUsageTags.clusterOwnerOrgId",
      "sparkContextId" -> "spark.databricks.sparkContextId",
      
      "clientName" -> "azure.data.monitoring.clientName",
      "retailerName" -> "azure.data.monitoring.retailerName",
      "pipelinePhase" -> "azure.data.monitoring.pipelinePhase",
      "notebookPath" -> "azure.data.monitoring.notebookPath"
    )
    
    val sparkSession = SparkSession.getDefaultSession
    val sparkValues = sparkKeys.mapValues(k => getSparkConfValue(sparkSession, k))
    properties ++= sparkValues
    properties.put("sparkSessionId", Some(sparkSession.toString))

    // Convert the property values we gathered to a format ready for the TelemetryClient
    val telemetryProperties = new HashMap[String, String]()
    for (key <- properties.keys)
      if (!properties(key).isEmpty)
        telemetryProperties.put(key, properties(key).get)
    
    return telemetryProperties
  }
  
  
  /*******************************************************************************
  *  EVENT HANDLERS
  *******************************************************************************/
  
  override def onJobStart(jobStart: SparkListenerJobStart) {
    val properties = buildProperties()
    properties.put("jobId", jobStart.jobId.toString)

    val metrics = new HashMap[String, java.lang.Double]()
    metrics.put("stageInfos.size", jobStart.stageInfos.size)
    metrics.put("time", jobStart.time)

    telemetryClient.trackEvent("onJobStart", properties, metrics)
  }
  
  
  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    val properties = buildProperties()
    properties.put("jobId", jobEnd.jobId.toString)
    properties.put("jobResult", jobEnd.jobResult.getClass.getSimpleName)

    val metrics = new HashMap[String, java.lang.Double]()
    metrics.put("time", jobEnd.time)

    telemetryClient.trackEvent("onJobEnd", properties, metrics)
  }
  
  
  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = { 
    val succeeded = stageCompleted.stageInfo.failureReason.isEmpty

    val properties = buildProperties()
    properties.put("stageId", stageCompleted.stageInfo.stageId.toString)
    properties.put("name", stageCompleted.stageInfo.name)
    properties.put("succeeded", succeeded.toString)
    
    if (!succeeded) {
      properties.put("failureReason", stageCompleted.stageInfo.failureReason.get)
    }

    val metrics = new HashMap[String, java.lang.Double]()
    metrics.put("attemptNumber", stageCompleted.stageInfo.attemptNumber)
    metrics.put("numTasks", stageCompleted.stageInfo.numTasks)
    metrics.put("executorDeserializeTime", stageCompleted.stageInfo.taskMetrics.executorDeserializeTime)
    metrics.put("executorDeserializeCpuTime", stageCompleted.stageInfo.taskMetrics.executorDeserializeCpuTime)
    metrics.put("executorRunTime", stageCompleted.stageInfo.taskMetrics.executorRunTime)
    metrics.put("resultSize", stageCompleted.stageInfo.taskMetrics.resultSize)
    metrics.put("jvmGCTime", stageCompleted.stageInfo.taskMetrics.jvmGCTime)
    metrics.put("resultSerializationTime", stageCompleted.stageInfo.taskMetrics.resultSerializationTime)
    metrics.put("memoryBytesSpilled", stageCompleted.stageInfo.taskMetrics.memoryBytesSpilled)
    metrics.put("diskBytesSpilled", stageCompleted.stageInfo.taskMetrics.diskBytesSpilled)
    metrics.put("peakExecutionMemory", stageCompleted.stageInfo.taskMetrics.peakExecutionMemory)
    
    telemetryClient.trackEvent("onStageCompleted", properties, metrics)
  }

  // This is a fake event that we call when the class is instantiated
  def onListenerRegister(): Unit = {
      val properties = buildProperties()
      val metrics = new HashMap[String, java.lang.Double]()

      telemetryClient.trackEvent("onListenerRegister", properties, metrics)
  }
}

// Companion object with util functions
object AdbxListener {
  def getInfoFromScala(): scala.collection.mutable.Map[String, String] = {
    val defaultSparkSession = SparkSession.getDefaultSession
    val activeSparkSession = SparkSession.getActiveSession
    val notebookContext = dbutils.notebook.getContext

    val output = scala.collection.mutable.Map(
      "Notebook Path" -> notebookContext.notebookPath.get.toString,
      "Active Spark Session" -> activeSparkSession.toString,
      "Default Spark Session" -> defaultSparkSession.toString
    )

    return output
  }

  def registerNotebookWithLogger(clientName: String, retailerName: String, pipelinePhase: String): String = {
    val sparkSession = SparkSession.getActiveSession
    val sparkConf = sparkSession.get.conf
    val notebookContext = dbutils.notebook.getContext

    sparkConf.set("azure.data.monitoring.clientName", clientName)
    sparkConf.set("azure.data.monitoring.retailerName", retailerName)
    sparkConf.set("azure.data.monitoring.pipelinePhase", pipelinePhase)
    sparkConf.set("azure.data.monitoring.notebookPath", notebookContext.notebookPath.get)

    return sparkSession.toString
  }
}
