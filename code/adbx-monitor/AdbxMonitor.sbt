name := "AdbxMonitor"

version := "0.1"

scalaVersion := "2.12.11"

// scalacOptions += "-Ylog-classpath"

libraryDependencies += "org.apache.spark" %% "spark-core" % "3.0.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.0.0"

// https://mvnrepository.com/artifact/com.microsoft.azure/applicationinsights-core
libraryDependencies += "com.microsoft.azure" % "applicationinsights-core" % "2.6.0"

// https://mvnrepository.com/artifact/com.databricks/dbutils-api
// libraryDependencies += "com.databricks" %% "dbutils-api" % "0.0.4"

// https://mvnrepository.com/artifact/com.typesafe.scala-logging/scala-logging-slf4j
// libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"

// https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
// libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.9"

// https://mvnrepository.com/artifact/com.typesafe/config
libraryDependencies += "com.typesafe" % "config" % "1.2.1"
