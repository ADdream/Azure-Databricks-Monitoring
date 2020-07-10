# You can run this on Windows as well, just change to a batch files.
#
# Note: You need the Databricks CLI installed, and you need a token configued.
#       You may need to add a "--profile" option to each command if you want to
#       upload to a Databricks workspace that is not configured as your default
#       profile for the Databricks CLI.
#
#!/bin/bash

echo "Creating DBFS direcrtory"
dbfs mkdirs dbfs:/databricks/appinsights

echo "Uploading App Insights JAR files for Log4J verison 1.2 (Spark currently uses 1.2)"
echo "To download new JARs go to: https://github.com/Microsoft/ApplicationInsights-Java/releases"
dbfs cp --overwrite applicationinsights-core-*.jar              dbfs:/databricks/appinsights/
dbfs cp --overwrite applicationinsights-logging-log4j1_2-*.jar  dbfs:/databricks/appinsights/

echo "Uploading custom Spark Listener library"
dbfs cp --overwrite adbxmonitor_*.jar                           dbfs:/databricks/appinsights/

echo "Uploading cluster init script"
dbfs cp --overwrite code/appinsights_logging_init.sh            dbfs:/databricks/appinsights/appinsights_logging_init.sh

echo "Listing DBFS directory"
dbfs ls dbfs:/databricks/appinsights
