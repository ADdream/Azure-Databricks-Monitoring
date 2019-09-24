// https://spark.apache.org/docs/2.1.0/api/java/org/apache/spark/scheduler/SparkListener.html
// https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/scheduler/SparkListenerJobStart.html
// https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/scheduler/SparkListenerJobEnd.html
// https://spark.apache.org/docs/2.2.0/api/java/org/apache/spark/scheduler/SparkListenerStageCompleted.html

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.TelemetryConfiguration
import org.apache.spark.scheduler._
import java.util._
import scala.collection.JavaConverters._

val configuration = com.microsoft.applicationinsights.TelemetryConfiguration.createDefault()
configuration.setInstrumentationKey(System.getenv("APPINSIGHTS_INSTRUMENTATIONKEY"))
val telemetryClient = new TelemetryClient(configuration)

class CustomListener extends SparkListener  {
  
  override def onJobStart(jobStart: SparkListenerJobStart) {
    val properties = new HashMap[String, String]()
    properties.put("jobId", jobStart.jobId.toString)
    properties.put("clusterId", spark.conf.get("spark.databricks.clusterUsageTags.clusterId"))
    properties.put("clusterName", spark.conf.get("spark.databricks.clusterUsageTags.clusterName"))
    

    val metrics = new HashMap[String, java.lang.Double]()
    metrics.put("stageInfos.size", jobStart.stageInfos.size)
    metrics.put("time", jobStart.time)

    telemetryClient.trackEvent("onJobStart", properties, metrics)
  }
  
  
  override def onJobEnd(jobEnd: SparkListenerJobEnd): Unit = {
    val properties = new HashMap[String, String]()
    properties.put("jobId", jobEnd.jobId.toString)
    properties.put("clusterId", spark.conf.get("spark.databricks.clusterUsageTags.clusterId"))
    properties.put("clusterName", spark.conf.get("spark.databricks.clusterUsageTags.clusterName"))
    properties.put("jobResult", jobEnd.jobResult.toStringStart.jobId.toString)

    val metrics = new HashMap[String, java.lang.Double]()
    metrics.put("time", jobEnd.time)

    telemetryClient.trackEvent("onJobEnd", properties, metrics)
  }
  
  
  override def onStageCompleted(stageCompleted: SparkListenerStageCompleted): Unit = { 

    val properties = new HashMap[String, String]()
    properties.put("stageId", stageCompleted.stageInfo.stageId.toString)
    properties.put("name", stageCompleted.stageInfo.name)
    properties.put("clusterId", spark.conf.get("spark.databricks.clusterUsageTags.clusterId"))
    properties.put("clusterName", spark.conf.get("spark.databricks.clusterUsageTags.clusterName"))

    val metrics = new HashMap[String, java.lang.Double]()
    metrics.put("attemptNumber", stageCompleted.stageInfo.attemptNumber)
    metrics.put("numTasks", stageCompleted.stageInfo.numTasks)
    //metrics.put("submissionTime", (stageCompleted.stageInfo.submissionTime.toString).toDouble)
    //metrics.put("completionTime", (stageCompleted.stageInfo.completionTime.toString).toDouble)
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
}

val myListener=new CustomListener
sc.addSparkListener(myListener)