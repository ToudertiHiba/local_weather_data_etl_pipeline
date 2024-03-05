
from kafka import KafkaProducer
import json

KAFKA_TOPIC = "weather"
KAFKA_BROKER = "localhost:9092"
API_KEY = "20229b2b71msh24e52dfd4f1c5f7p13a78cjsnad34c56afa3f"
# Replace with the weather API URL
API_URL = "https://weatherapi-com.p.rapidapi.com/current.json"


def fetch_weather_data(city_name):
    import requests
    response = requests.get(API_URL, params={"q": city_name}, headers={
                            "X-RapidAPI-Key": API_KEY})
    return response.json()


def main():
    # List of cities for which you want weather data
    cities = ["Paris", "London", "New York", "Tokyo", "Sydney"]
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BROKER,
        value_serializer=lambda v: json.dumps(v).encode("utf-8")
    )

    while True:
        for city in cities:
            weather_data = fetch_weather_data(city)
            producer.send(KAFKA_TOPIC, value=weather_data)
            producer.flush()


if __name__ == "__main__":
    main()
