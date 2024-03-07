
from kafka import KafkaProducer
import json
import requests

KAFKA_TOPIC = "weather"
KAFKA_BROKER = "localhost:9092"

class WeatherDataFetcher:
    def __init__(self, api_key: str, api_url: str):
        self.api_key = api_key
        self.api_url = api_url

    def fetch_weather_data(self, city_name: str) -> dict:
        response = requests.get(self.api_url, params={"q": city_name}, headers={
                                "X-RapidAPI-Key": self.api_key})
        response.raise_for_status()  # Raise an exception if the request fails
        return response.json()

def main():
    # List of cities for which you want weather data
    cities = ["Paris", "London", "New York", "Tokyo", "Sydney"]
    with open('api_config.json') as f:
        config = json.load(f)
        weather_data_fetcher = WeatherDataFetcher(
            api_key=config['API_KEY'],
            api_url=config['API_URL']
        )
        
    producer = KafkaProducer(
        bootstrap_servers=KAFKA_BROKER,
        value_serializer=lambda v: json.dumps(v).encode("utf-8")
    )

    while True:
        for city in cities:
            weather_data = weather_data_fetcher.fetch_weather_data(city)
            producer.send(KAFKA_TOPIC, value=weather_data)
            producer.flush()


if __name__ == "__main__":
    main()
