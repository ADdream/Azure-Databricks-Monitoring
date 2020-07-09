# Azure Databricks Monitoring

Azure Databricks has some [native integration with Azure Monitor](https://docs.microsoft.com/en-us/azure/databricks/administration-guide/account-settings/azure-diagnostic-logs)
that allows customers to track workspace-level events in Azure Monitor.  However, many customers
want a deeper view of the activity within Databricks.  This repo presents a solution that
will send much more detailed information about the Spark jobs running on Databricks clusters
over to Azure Monitor.  This allows organizations to use the same reporting and alerting features
in Azure Monitor that they use for other services to be applied to activities within Databricks.

This solution includes two distinct components:
1. Copy Spark logs managed by Log4J to Log Analytics, which will help you get your
logs to a centralized location.

2. Send both standard and custom metrics about Spark jobs to Application Insights.  This is
accomplished by attaching a custom Spark Listener to your Databricks clusters.

These two components can be implemented independently of each other, based on your needs.  It
should be noted that Log4J is quite verbose, and for large Databricks deployments, it can be
expensive to import so much logging data into Log Analytics.

## Setup
Follow the steps below to configure the solution in your environment.

### Configuration Steps: Application Insights
1. Create Application Insights in Azure 
1. Get your instrumentation key on the overview page
1. Enter `APPINSIGHTS_INSTRUMENTATIONKEY` in the `appinsights_logging_init.sh` script

### Configuration Steps: Log Analytics
1. Create a Log Analytics account in Azure
1. Get your workspace id on the overview page
1. Get your primary key by clicking Advanced Settings >> Connected Sources >> Linux and copy primary key
1. Enter `LOG_ANALYTICS_WORKSPACE_ID` in the `appinsights_logging_init.sh` script
1. Enter `LOG_ANALYTICS_PRIMARY_KEY` in the `appinsights_logging_init.sh` script
1. Get your primary key by clicking Advanced Settings >> Data >> Linux Performace Counters and click "Apply below configuration to my machines" then press Save
1. Click the Add button (The UI should turn to a grid) then press Save

### Configuration Steps: Databricks
1. Create Databricks workspace in Azure
1. Install Databricks CLI on your local machine
1. Open your Azure Databricks workspace, click on the user icon, and create a token
1. Run `databricks configure --token` on your local machine to configure the Databricks CLI
1. Run `Upload-Items-To-Databricks.sh`
   - Change the extension to `.bat` for Windows).
   - On Linux you will need to do a `chmod +x` on this file to run.

   This will copy the `.jar` files and init script from this repo to the DBFS in your Databricks
   workspace.

1. Create a cluster in Databricks (any size and shape is fine)
    - Make sure you click Advanced Options and "Init Scripts"
    - Add a script for "dbfs:/databricks/appinsights/appinsights_logging_init.sh"
    ![alt tag](https://raw.githubusercontent.com/AnalyticJeremy/Azure-Databricks-Monitoring/master/images/databrickscluster.png)
1. Start the cluster    
1. *OPTIONAL* Install the `applicationsights` Python package
   [from PyPi](https://pypi.org/project/applicationinsights/) to the cluster.
    - This provides the ability to send custom events and metrics to app insights.
    - You'll need to follow this step if you plan on logging Custom Metrics or Events to App Insights on Pyspark.
    - Steps to [install a library](https://docs.azuredatabricks.net/user-guide/libraries.html#install-libraries) on Azure Databricks

## Verification Steps
1. Import the notebooks in the `AppInsightsTest.dbc` file
1. Run the AppInsightsTest Scala notebook
    1. Cell 1 displays your application insights key
    1. Cell 2 displays your jars (application insights jars should be in here)
    1. Cell 3 displays your log4j.properities file on the "driver" (which has the aiAppender)
    1. Cell 4 displays your log4j.properities file on the "executor" (which has the aiAppender)
    1. Cell 5 writes to Log4J so the message will appear in App Insights
    1. Cell 6 writes to App Insights via the App Insights API.  This will show as a "Custom Event" (customEvents table).
1. *OPTIONAL* Run the AppInsightsPython Python notebook
    1. Cell 1 creates a reference to the Log4J logger (called aiAppender) and writes to Log4J so the message will appear in App Insights.
    1. Cell 2 configures the connection to App Insights via the `appinsights` package.
    1. Cell 3 writes to App Insights via the App Insights API. This will show as a "Custom Event" (customEvents table).
1. Open your App Insights account in the Azure Portal
1. Click on Search (top bar or left menu)
1. Click Refresh (over and over until you see data)
    - For a new App Insights account this can take 10 to 15 minutes to really initialize
    - For an account that is initialized expect a 1 to 3 minute delay for telemetry

## Now That You Have Data...
1. The data will come into App Insights as a Trace
![alt tag](https://raw.githubusercontent.com/AnalyticJeremy/Azure-Databricks-Monitoring/master/images/dimensiondata.png)

1. This means the data will be in the customDimensions field as a property bag
1. Open the Analytic query for App Insights
1. Run ``` traces | order by timestamp desc ```
   - You will notice how customDimensions contains the fields 
1. Parse the custom dimensions.  This will make the display easier.
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

![alt tag](https://raw.githubusercontent.com/AnalyticJeremy/Azure-Databricks-Monitoring/master/images/formatteddata.png)

1. Run ``` customEvents | order by timestamp  desc ``` to see the custom event your Notebook wrote
1. Run ``` customMetrics | order by timestamp  desc ``` to see the HeartbeatState
1. Don't know which field has your data: ``` traces | where * contains "App Insights on Databricks"    ```
1. Open your Log Analytics account
   1. Click on Logs
   1. Write a query against the Perf and/or Heartbeat tables
   ![alt tag](https://raw.githubusercontent.com/AnalyticJeremy/Azure-Databricks-Monitoring/master/images/perfdata.png)

## Logging each Spark Job to Application Insights Automatically

Install the custom Scala Listener on your cluster to automatically send Spark job events to Application Insights.


## Things You Can Do...
1. For query help see: https://docs.microsoft.com/en-us/azure/kusto/query/
1. Show this data in Power BI: https://docs.microsoft.com/en-us/azure/azure-monitor/app/export-power-bi
1. You can pin your queries to an Azure Dashboard: https://docs.microsoft.com/en-us/azure/azure-monitor/app/app-insights-dashboards
1. You can configure continuous export your App Insights data and send to other systems. Create a Stream Analytics job to monitor the exported blob location and send from there.
1. Set up alerts: https://docs.microsoft.com/en-us/azure/azure-monitor/platform/alerts-log-query
1. You can get JMX metrics: https://docs.microsoft.com/en-us/azure/azure-monitor/app/java-get-started#performance-counters.  You will need an ApplicationInsights.XML file: https://github.com/Microsoft/ApplicationInsights-Java/wiki/ApplicationInsights.XML.  You probably need to upload this to DBFS and then copy in the appinsights_logging_init.sh to the cluster. (I have not yet tested this setup.)