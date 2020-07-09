# the `lib` directory

To compile the custom Spark listener into a Scala library, you will need to
link to some libraries used by Databricks.  To do this, just copy the following
`.jar` files from your Databricks environment and put them in this `lib`
directory:

- `----jackson_databind_shaded--libjackson-databind.jar`
- `chauffeur-api--chauffeur-api-spark_3.0_2.12_deploy.jar`
- `common--common-spark_3.0_2.12_deploy.jar`
- `extern--extern-spark_3.0_2.12_deploy.jar`
- `jsonutil--jsonutil-spark_3.0_2.12_deploy.jar`

---

Here's the easiest way to accomplish this.  First, create a new Databricks
notebook and paste the code below into a cell.  This will copy the `.jar` files
from the driver node to the DBFS.
```
%sh
mkdir -p /dbfs/jar-transfer
rm -r /dbfs/jar-transfer/*
cp /databricks/jars/----jackson_databind_shaded--libjackson-databind.jar /dbfs/jar-transfer
cp /databricks/jars/chauffeur-api--chauffeur-api-spark_*_deploy.jar /dbfs/jar-transfer
cp /databricks/jars/common--common-spark_*_deploy.jar /dbfs/jar-transfer
cp /databricks/jars/extern--extern-spark_*_deploy.jar /dbfs/jar-transfer
cp /databricks/jars/jsonutil--jsonutil-spark_*_deploy.jar /dbfs/jar-transfer

ls -l /dbfs/jar-transfer
```

Now that the `.jar` files are in the DBFS, we can use the Databricks CLI to copy
the files to our local workstation.

```
dbfs cp -r dbfs:/jar-transfer ./code/adbx-monitor/lib
```

You are now ready to build the Scala library with SBT!