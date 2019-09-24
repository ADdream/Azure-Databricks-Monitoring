# the `lib` directory

To compile the Scala library with the custom Spark listener, you will need to
link to some libraries used by Databricks.  To do this, just copy the following
`.jar` files from your Databricks environment and put them in this `lib`
directory:

- `----jackson_databind_shaded--libjackson-databind.jar`
- `chauffeur-api--chauffeur-api-spark_2.4_2.11_deploy.jar`
- `common--common-spark_2.4_2.11_deploy.jar`
- `extern--extern-spark_2.4_2.11_deploy.jar`
- `jsonutil--jsonutil-spark_2.4_2.11_deploy.jar`