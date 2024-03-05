import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming._
import org.apache.spark.sql.types._

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import com.datastax.oss.driver.api.core.uuid.Uuids
import com.datastax.spark.connector._
import org.apache.spark.sql.cassandra._

case class WeatherData(city: String, temp: Double, humid: Double, pres: Double)

object StreamHandler {
    def main(args: Array[String]): Unit =  {
      val spark = SparkSession
            .builder()
            .appName("StreamHandler")
            .config("spark.cassandra.connection.host", "localhost")
            .config("spark.cassandra.connection.port", "9042")
            .getOrCreate()
      
      import spark.implicits._

      // Define Kafka topic and broker address
      val kafkaTopic = "weather" // Replace with your Kafka topic name
      val kafkaBrokers = "localhost:9092" // Replace with your Kafka broker address

      // Read data from Kafka using Spark Structured Streaming : this is the input dataframe
      val kafkaStream = spark.readStream
        .format("kafka")
        .option("kafka.bootstrap.servers", kafkaBrokers)
        .option("subscribe", kafkaTopic)
        .option("startingOffsets", "earliest") // From starting
        .load()

      kafkaStream.printSchema()

      // Process the streaming DataFrame
      val jsonData = kafkaStream.selectExpr("CAST(value AS STRING)")
    
    
      // Define the schema to match your JSON data structure
      val schema = StructType(Seq(
        StructField("location", StructType(Seq(
          StructField("name", StringType),
          StructField("region", StringType),
          StructField("country", StringType),
          StructField("lat", DoubleType),
          StructField("lon", DoubleType),
          StructField("tz_id", StringType),
          StructField("localtime_epoch", LongType),
          StructField("localtime", StringType)
        ))),
        StructField("current", StructType(Seq(
          StructField("last_updated_epoch", LongType),
          StructField("last_updated", StringType),
          StructField("temp_c", DoubleType),
          StructField("temp_f", DoubleType),
          StructField("is_day", IntegerType),
          StructField("condition", StructType(Seq(
            StructField("text", StringType),
            StructField("icon", StringType),
            StructField("code", IntegerType)
          ))),
          StructField("wind_mph", DoubleType),
          StructField("wind_kph", DoubleType),
          StructField("wind_degree", DoubleType),
          StructField("wind_dir", StringType),
          StructField("pressure_mb", DoubleType),
          StructField("pressure_in", DoubleType),
          StructField("precip_mm", DoubleType),
          StructField("precip_in", DoubleType),
          StructField("humidity", IntegerType),
          StructField("cloud", IntegerType),
          StructField("feelslike_c", DoubleType),
          StructField("feelslike_f", DoubleType),
          StructField("vis_km", DoubleType),
          StructField("vis_miles", DoubleType),
          StructField("uv", DoubleType),
          StructField("gust_mph", DoubleType),
          StructField("gust_kph", DoubleType)
        )))
      ))

    // Parse JSON data and convert to DataFrame
    val parsedData = jsonData.select(from_json(col("value"), schema).as("data"))
      .select("data.*")
 
    
    // Select the required fields "location.name", "current.temp_c", and "current.humidity"
    val resultDF = parsedData.select($"location.localtime".alias("localtime"), $"location.name".alias("cityname"), $"current.temp_c".alias("temperature"), $"current.humidity", $"current.pressure_mb".alias("pressure"))
    val filteredDF = resultDF.filter(resultDF("localtime").isNotNull && resultDF("cityname").isNotNull && resultDF("temperature").isNotNull && resultDF("humidity").isNotNull && resultDF("pressure").isNotNull)
    
    val makeUUID = udf(() => Uuids.timeBased().toString)
    val resultWithIDs = filteredDF.withColumn("uuid", makeUUID())
    // Define the Cassandra table schema

    val cassandraTableSchema = StructType(Seq(
      StructField("cityname", StringType),
      StructField("localtime", StringType),
      StructField("temperature", DoubleType),
      StructField("humidity", IntegerType),
      StructField("pressure", DoubleType)
    ))


    // Write the result DataFrame to the console
    val query = resultWithIDs
      .writeStream
      .trigger(Trigger.ProcessingTime("15 seconds"))
      .foreachBatch{ (batchDF: DataFrame, batchID: Long) =>
        batchDF.write
          .cassandraFormat("weather_data", "weather_project")
          .mode(SaveMode.Append)
          .save()
      }
      .start()

    query.awaitTermination()
    
  }
}
