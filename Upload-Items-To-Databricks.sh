# You can run this on Windows as well, just change to a batch files
# Note: You need the Databricks CLI installed and you need a token configued
#!/bin/bash

echo "Creating DBFS direcrtory"
dbfs mkdirs dbfs:/databricks/appinsights

echo "Uploading App Insights JAR files for Log4J verison 1.2 (Spark currently uses 1.2)"
echo "To download new JARs go to: https://github.com/Microsoft/ApplicationInsights-Java/releases"
dbfs cp --overwrite applicationinsights-core-2.6.1.jar              dbfs:/databricks/appinsights/applicationinsights-core-2.6.1.jar
dbfs cp --overwrite applicationinsights-logging-log4j1_2-2.6.1.jar  dbfs:/databricks/appinsights/applicationinsights-logging-log4j1_2-2.6.1.jar

echo "Uploading custom Spark Listener library"
dbfs cp --overwrite adbxmonitor_2.12-0.1.jar                        dbfs:/databricks/appinsights/adbxmonitor_2.12-0.1.jar

echo "Uploading cluster init script"
dbfs cp --overwrite code/appinsights_logging_init.sh                dbfs:/databricks/appinsights/appinsights_logging_init.sh

echo "Listing DBFS directory"
dbfs ls dbfs:/databricks/appinsights