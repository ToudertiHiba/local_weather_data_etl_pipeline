## Installation Guide

### 1. Install Java JDK 8
   - Download the Java SE Development Kit 8 from [here](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html).
   - Add `JAVA_HOME` and `JAVA_HOME\bin` to the PATH in environment variables.
   - Execute `echo $env:JAVA_HOME` and `java -version` to verify the installation.

### 2. Start Zookeeper and Kafka brokers
   - Follow the instructions provided in the [Kafka Quickstart Guide](https://kafka.apache.org/quickstart).
   - Modify the `config/server.properties` file.
   ```properties
   listeners=PLAINTEXT://:9092
   advertised.listeners=PLAINTEXT://localhost:9092
   zookeeper.connect=localhost:2181
   ```
   - Run the following commands:
     ```
     .\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties
     .\bin\windows\kafka-server-start.bat .\config\server.properties
     bin/windows/kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic weather --from-beginning
     python .\kafka_producer.py
     python .\kafka_consumer.py
     ```

### 3. Setting Up Spark (Scala)
   - Install Sbt from [here](https://www.scala-sbt.org/download/).
   - Install Scala: Version 2.11 or 2.12 is required. Download and install Scala from [here](https://www.scala-lang.org/download/2.13.13.html).
   - Install Spark: Download the Spark distribution matching your Scala version from [here](https://spark.apache.org/downloads.html).
   - Set Environment Variables: Add Spark and Scala installation directories to PATH. Set `SPARK_HOME`, `SBT_HOME`, and `SCALA_HOME` environment variables accordingly.

#### Install Hadoop:
   - Install the `winutils.exe` file from [here](https://github.com/cdarlint/winutils/blob/master/hadoop-3.3.5/bin/winutils.exe) and hadoop.dll from [here](https://github.com/cdarlint/winutils/blob/master/hadoop-3.3.5/bin/hadoop.dll)
   - Place it inside the bin directory of the Hadoop directory.
   - Define `hadoop_home` as the Hadoop directory.

#### Connecting Spark to Kafka 
   - Run `sbt package`.
   - Execute:
     ```
     spark-submit --class StreamHandler --master local[*] target/scala-2.13/streamhandler_2.13-1.0.jar
     ```
   - This will fail because we need the packages (the connector between Spark and Kafka).
   - Execute:
     ```
     spark-submit --class StreamHandler --master local[*] --packages org.apache.spark:spark-sql-kafka-0-10_2.13:3.5.1 target/scala-2.13/streamHandler_2.13-1.0.jar
     ```

### 4. Installing Cassandra
   - Install from [here](https://downloads.apache.org/cassandra/3.11.16/) and [here](https://downloads.apache.org/cassandra/4.1.4/). Copy the necessary files from version 3 to version 4 and use version 4 for environment paths.
   - Set environment variables: `cassandra_home`, etc.
   - Execute:
     ```
     ./bin/cassandra.bat -f
     ./bin/nodetool status
     ./bin/cqlsh.bat
     ```

#### Create Keyspace and Table in Cassandra
   ```
   describe keyspaces;
   create keyspace weather_project with replication = {'class': 'SimpleStrategy', 'replication_factor': 1};
   use weather_project;

   CREATE TABLE IF NOT EXISTS weather_project.weather_data (
      localtime text,
      cityname text,
      temperature double,
      humidity int,
      pressure double,
      uuid uuid,
      PRIMARY KEY (uuid)
   );
   ```

#### Connecting Spark to Cassandra:
   - Execute:
     ```
     spark-submit --class StreamHandler --master local[*] --packages org.apache.spark:spark-sql-kafka-0-10_2.13:3.5.1,com.datastax.oss:java-driver-core:4.13.0,com.datastax.spark:spark-cassandra-connector_2.13:3.5.0 target/scala-2.13/streamhandler_2.13-1.0.jar
     ```

### Using Streamlit for Visualization:
   - Install the cassandra-driver using poetry: `poetry add cassandra-driver plotly streamlit pandas`.
   - Execute: 
      ```
      streamlit run .\streamlit_visualisation.py
      ```