name := "DataPlatMonitor" 
 
version := "0.1" 
 
scalaVersion := "2.11.12" 

// scalacOptions += "-Ylog-classpath"

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.4.0"

// https://mvnrepository.com/artifact/com.microsoft.azure/applicationinsights-core
libraryDependencies += "com.microsoft.azure" % "applicationinsights-core" % "2.4.0-BETA"

// https://mvnrepository.com/artifact/com.databricks/dbutils-api
libraryDependencies += "com.databricks" %% "dbutils-api" % "0.0.3"

// https://mvnrepository.com/artifact/com.typesafe.scala-logging/scala-logging-slf4j
// libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
// libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9"

// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.2.1"
