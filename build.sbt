name := "StreamHandler"

version := "1.0"
scalaVersion := "2.13.13"

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "3.5.1" % "provided",
    "org.apache.spark" %% "spark-sql" % "3.5.1" % "provided",
    "com.datastax.spark" %% "spark-cassandra-connector" % "3.5.0",
    "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.5.1"
)

libraryDependencies += "com.datastax.oss" % "java-driver-core" % "4.13.0" pomOnly()