
package com.jakeer.transactionservice.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringJUnitConfig
@EmbeddedKafka(
        partitions = 1,
        topics = "offset-replay-test"
)
class KafkaOffsetReplayIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker broker;

    @Test
    void uncommittedMessageShouldReplayAfterConsumerRestart()
            throws Exception {

        String topic = "offset-replay-uncommitted-test";

        String groupId = "replay-" + UUID.randomUUID();

        // Step 1: Configure Kafka producer
        Properties producerProps = new Properties();

        producerProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                broker.getBrokersAsString()
        );
        producerProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        producerProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        // Step 2: Publish transaction event
        try (KafkaProducer<String, String> producer =
                     new KafkaProducer<>(producerProps)) {

            producer.send(
                    new ProducerRecord<>(
                            topic,
                            "TXN-101",
                            "APPROVE"
                    )
            ).get();
        }

        // Step 3: Configure Kafka consumer
        Properties consumerProps = new Properties();

        consumerProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                broker.getBrokersAsString()
        );
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );
        consumerProps.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        consumerProps.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        consumerProps.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );
        consumerProps.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        // Step 4: Consumer A reads without committing
        try (KafkaConsumer<String, String> consumerA =
                     new KafkaConsumer<>(consumerProps)) {

            consumerA.subscribe(List.of(topic));

            ConsumerRecords<String, String> records =
                    pollUntilMessage(consumerA);

            assertEquals(1, records.count());
            assertEquals(
                    "APPROVE",
                    records.iterator().next().value()
            );
        }

        // Step 5: Consumer B restarts with same group
        try (KafkaConsumer<String, String> consumerB =
                     new KafkaConsumer<>(consumerProps)) {

            consumerB.subscribe(List.of(topic));

            ConsumerRecords<String, String> records =
                    pollUntilMessage(consumerB);

            assertEquals(1, records.count());
            assertEquals(
                    "APPROVE",
                    records.iterator().next().value()
            );
        }
    }

    // Helper method: Poll until Kafka message arrives
    private ConsumerRecords<String, String> pollUntilMessage(
            KafkaConsumer<String, String> consumer) {

        long deadline = System.currentTimeMillis() + 15000;

        while (System.currentTimeMillis() < deadline) {

            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(500));

            if (!records.isEmpty()) {
                return records;
            }
        }

        throw new AssertionError("No Kafka message received");
    }

    @Test
    void committedMessageShouldNotReplayAfterConsumerRestart()
            throws Exception {

        String topic = "offset-replay-committed-test";
        String groupId = "committed-" + UUID.randomUUID();

        Properties producerProps = new Properties();
        producerProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                broker.getBrokersAsString()
        );
        producerProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );
        producerProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        try (KafkaProducer<String, String> producer =
                     new KafkaProducer<>(producerProps)) {

            producer.send(
                    new ProducerRecord<>(topic, "TXN-102", "APPROVE")
            ).get();
        }

        Properties consumerProps = new Properties();
        consumerProps.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                broker.getBrokersAsString()
        );
        consumerProps.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId
        );
        consumerProps.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        consumerProps.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        consumerProps.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );
        consumerProps.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        // Consumer A processes and commits
        try (KafkaConsumer<String, String> consumerA =
                     new KafkaConsumer<>(consumerProps)) {

            consumerA.subscribe(List.of(topic));

            var records = pollUntilMessage(consumerA);

            assertEquals(1, records.count());
            assertEquals("APPROVE", records.iterator().next().value());

            consumerA.commitSync();
        }

        // Consumer B starts with same group
        try (KafkaConsumer<String, String> consumerB =
                     new KafkaConsumer<>(consumerProps)) {

            consumerB.subscribe(List.of(topic));

            // Allow group assignment and check for old messages
            boolean receivedOldMessage = false;
            long deadline = System.currentTimeMillis() + 3000;

            while (System.currentTimeMillis() < deadline) {
                var records = consumerB.poll(Duration.ofMillis(300));

                if (!records.isEmpty()) {
                    receivedOldMessage = true;
                    break;
                }
            }

            assertFalse(
                    receivedOldMessage,
                    "Committed message should not replay"
            );
        }
    }
}
