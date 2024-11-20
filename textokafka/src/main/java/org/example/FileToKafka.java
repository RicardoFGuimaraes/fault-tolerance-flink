package org.example;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Properties;
import java.util.Scanner;

public class FileToKafka {
    public static void main(String[] args) throws Exception {
        long runTimeSec = Long.parseLong(args[0]);
        String filePath = "/home/book.dat";
        String topicName = "books";
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);
        Callback callback = (metadata, exception) -> {
            if (exception == null) {
                System.out.println("Message sent successfully: " + metadata.toString());
            } else {
                System.out.println(exception.getMessage());
                exception.printStackTrace();
            }
        };

        long epoch = System.nanoTime();
        while (System.nanoTime() - epoch < runTimeSec * 1e9) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = br.readLine()) != null) {
                    producer.send(new ProducerRecord<>(topicName, line), callback);
                }
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        producer.close();
    }
}
