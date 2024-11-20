from kafka import KafkaProducer
import time

# Path to your .dat file
file_path = '/c/Users/User/Downloads/DSPBench-master/DSPBench-master/dspbench-flink/data/books.dat'
kafka_topic = 'flink-events'
bootstrap_servers = 'localhost:9092'

# Create a Kafka producer
producer = KafkaProducer(bootstrap_servers=bootstrap_servers)

# Open the file and read line by line
with open(file_path, 'r') as file:
    for line in file:
        producer.send(kafka_topic, line.strip().encode('utf-8'))
        print(f'Sent: {line.strip()}')
        # time.sleep(1)  # Optional: add delay to simulate streaming

producer.flush()
producer.close()
