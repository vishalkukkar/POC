package com.example;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class Producer
{
    public static void main(String[] args) {

        System.out.println("Started-----------------------");
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // props.put("acks", "all");
        // props.put("retries", 0);
        // props.put("metadata.fetch.timeout.ms", 120000); // 2 minutes timeout
        // props.put("request.timeout.ms", 120000); // 2 minutes timeout


        // Create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        try {
            // Send 10 messages
            for (int i = 0; i < 10; i++) {
                ProducerRecord<String, String> record = new ProducerRecord<>("test-topic", "key-" + i, "message-" + i);
                RecordMetadata metadata = producer.send(record).get();
                System.out.printf("Sent record(key=%s value=%s) meta(partition=%d, offset=%d)\n",
                        record.key(), record.value(), metadata.partition(), metadata.offset());
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }

        System.out.println("End-----------------------");
    }
}