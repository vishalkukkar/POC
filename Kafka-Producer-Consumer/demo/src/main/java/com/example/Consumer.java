package com.example;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class Consumer {
    public static void main(String[] args) {
        System.out.println("Started-----------------------");

        // Configure consumer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("group.id", "test-group");
        props.put("auto.offset.reset", "earliest"); // Start consuming from the beginning of the topic

        // Create Kafka consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // Subscribe to the topic
        consumer.subscribe(Collections.singletonList("test-topic"));

        try {
            while (true) {
                // Poll for new records
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("Received record(key=%s value=%s) meta(partition=%d, offset=%d)\n",
                            record.key(), record.value(), record.partition(), record.offset());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }

        System.out.println("End-----------------------");
    }
}
