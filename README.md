# Azure-Databricks-Log4J-To-AppInsights
Connect your Spark Databricks clusters Log4J output to the Application Insights Appender.  This will help you get your logs to a centralized location such as App Insights.  Many of my customers have been asking for this along with getting the Spark job data from the cluster (that will be a future project).

I also added Log Analytics so that the server metrics will be captured and placed in Azure Monitor.

## Configuration Steps: Application Insights
1. Create Application Insights in Azure 
2. Get your instrumentation key on the overview page
2. Replace APPINSIGHTS_INSTRUMENTATIONKEY in the appinsights_logging_init.sh script

## Configuration Steps: Log Analytics
1. Create a Log Analytics account in Azure
2. Get your workspace id on the overview page
3. Get your primary key by clicking Advanced Settings | Connected Sources | Linux and copy primary key
4. Replace LOG_ANALYTICS_WORKSPACE_ID in the appinsights_logging_init.sh script
5. Replace LOG_ANALYTICS_PRIMARY_KEY in the appinsights_logging_init.sh script
6. Get your primary key by clicking Advanced Settings | Data | Linux Performace Counters and click "Apply below configuration to my machines" then press Save
7. Click the Add button (The UI should turn to a grid) then press Save

### Configuration Steps: Databricks
1. Create Databricks workspace in Azure
2. Install Databricks CLI
3. Open your Azure Databricks workspace, click on the user icon and create a token
4. Run "databricks configure --token" to configure the Databricks CLI
5. Run Upload-Items-To-Databricks.sh (change the .bat for Windows).  Linux you need to do a chmod +x on this file to run.
6. Create a cluster in Databricks (any size and shape is fine)
    - Make sure you click Advanced Options and "Init Scripts"
    - Add a script for "dbfs:/databricks/appinsights/appinsights_logging_init.sh"
    ![alt tag](https://raw.githubusercontent.com/AdamPaternostro/Azure-Databricks-Log4J-To-AppInsights/master/images/databrickscluster.png)
7. Start the cluster    
8. <OPTIONAL> Install the `applicationsights` Python package [from PyPi](https://pypi.org/project/applicationinsights/) to the cluster.
    - This provides the ability to send custom events and metrics to app insights.
    - You'll need to follow this step if you plan on logging Custom Metrics or Events to App Insights on Pyspark.
    - Steps to [install a library](https://docs.azuredatabricks.net/user-guide/libraries.html#install-libraries) on Azure Databricks

## Verification Steps
1. Import the notebooks in AppInsightsTest
1. Run the AppInsightsTest Scala notebook
    1. Cell 1 displays your application insights key
    2. Cell 2 displays your jars (application insights jars should be in here)
    3. Cell 3 displays your log4j.properities file on the "driver" (which has the aiAppender)
    4. Cell 4 displays your log4j.properities file on the "executor" (which has the aiAppender)
    5. Cell 5 writes to Log4J so the message will appear in App Insights
    6. Cell 6 writes to App Insights via the App Insights API.  This will show as a "Custom Event" (customEvents table).
1. <OPTIONAL> Run the AppInsightsPython Python notebook
    1. Cell 1 creates a reference to the Log4J logger (called aiAppender) and writes to Log4J so the message will appear in App Insights.
    1. Cell 2 configures the connection to App Insights via the `appinsights` package.
    1. Cell 3 writes to App Insights via the App Insights API. This will show as a "Custom Event" (customEvents table).
1. Open your App Insights account in the Azure Portal
1. Click on Search (top bar or left menu)
1. Click Refresh (over and over until you see data)
    - For a new App Insights account this can take 10 to 15 minutes to really initialize
    - For an account that is initialized expect a 1 to 3 minute delay for telemetry

## Now that you have data
1. The data will come into App Insights as a Trace
![alt tag](https://raw.githubusercontent.com/AdamPaternostro/Azure-Databricks-Log4J-To-AppInsights/master/images/dimensiondata.png)

2. This means the data will be in the customDimensions field as a property bag
3. Open the Analytic query for App Insights
4. Run ``` traces | order by timestamp desc ```
   - You will notice how customDimensions contains the fields 
5. Parse the custom dimensions.  This will make the display easier.
```
traces 
| project 
  message,
  severityLevel,
  LoggerName=customDimensions["LoggerName"], 
  LoggingLevel=customDimensions["LoggingLevel"],
  SourceType=customDimensions["SourceType"],
  ThreadName=customDimensions["LoggingLevel"],
  SparkTimestamp=customDimensions["TimeStamp"],
  timestamp 
| order by timestamp desc
```

![alt tag](https://raw.githubusercontent.com/AdamPaternostro/Azure-Databricks-Log4J-To-AppInsights/master/images/formatteddata.png)

6. Run ``` customEvents | order by timestamp  desc ``` to see the custom event your Notebook wrote
7. Run ``` customMetrics | order by timestamp  desc ``` to see the HeartbeatState
8. Don't know which field has your data: ``` traces | where * contains "App Insights on Databricks"    ```
9. Open your Log Analytics account
   1. Click on Logs
   2. Write a query against the Perf and/or Heartbeat tables
   ![alt tag](https://raw.githubusercontent.com/AdamPaternostro/Azure-Databricks-Log4J-To-AppInsights/master/images/perfdata.png)

## Logging each Spark Job to Application Insights automatically

Install the custom Scala listener on your cluster to automatically send Spark job events to Application Insights.


## Things you can do
1. For query help see: https://docs.microsoft.com/en-us/azure/kusto/query/
2. Show this data in Power BI: https://docs.microsoft.com/en-us/azure/azure-monitor/app/export-power-bi
3. You can pin your queries to an Azure Dashboard: https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-insights-dashboards
4. You can configure continuous export your App Insights data and send to other systems. Create a Stream Analytics job to monitor the exported blob location and send from there.
5. Set up alerts: https://docs.microsoft.com/en-us/azure/azure-monitor/platform/alerts-log-query
6. You can get JMX metrics: https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-get-started#performance-counters.  You will need an ApplicationInsights.XML file: https://github.com/Microsoft/ApplicationInsights-Java/wiki/ApplicationInsights.XML.  You probably need to upload this to DBFS and then copy in the appinsights_logging_init.sh to the cluster.  I have not yet tested this setup.
