from kafka import KafkaConsumer
import json

KAFKA_TOPIC = "weather"
KAFKA_BROKER = "localhost:9092"

consumer = KafkaConsumer(
    KAFKA_TOPIC,
    bootstrap_servers=KAFKA_BROKER,
    value_deserializer=lambda m: json.loads(m.decode("utf-8"))
)

for message in consumer:
    print(f"Received message: {message.value}")