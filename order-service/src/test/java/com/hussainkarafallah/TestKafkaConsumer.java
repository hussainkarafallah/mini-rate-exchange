package com.hussainkarafallah;
/* 
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.hussainkarafallah.messaging.KafkaTopics;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestKafkaConsumer {
    private final Map<String, List<byte[]>> receivedMessages = new HashMap<>();
    private final Consumer<String, byte[]> consumer;
    protected Logger log = LoggerFactory.getLogger(BaseIntTest.class);


    public TestKafkaConsumer() {
        // Kafka consumer properties
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9093");
        props.put("group.id", "test-group");
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", ByteArrayDeserializer.class.getName()); 

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(List.of(KafkaTopics.FULFILLMENT_MATCHED, KafkaTopics.ORDER_UPDATE_TOPIC));

        new Thread(this::consumeMessages).start();
    }

    private void consumeMessages() {
        try {
            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, byte[]> record : records) {
                    List<byte[]> messages = receivedMessages.computeIfAbsent(record.topic(), k -> new ArrayList<>());
                    messages.add(record.value());
                    log.info("Received message: topic = %s, partition = %d, offset = %d, key = %s, value = %s%n",
                            record.topic(), record.partition(), record.offset(), record.key(), Arrays.toString(record.value()));
                }
            }
        } finally {
            consumer.close();
        }
    }

    public void reset() {
        receivedMessages.clear();
    }

    // Getter method to access receivedMessages map
    public List<byte[]> getReceivedMessages(String topic) {
        return receivedMessages.getOrDefault(topic , List.of());
    }
}
*/

import org.springframework.stereotype.Component;

@Component
public class TestKafkaConsumer{

}