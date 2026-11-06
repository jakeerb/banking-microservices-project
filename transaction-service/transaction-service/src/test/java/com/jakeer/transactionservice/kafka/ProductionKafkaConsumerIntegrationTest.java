
package com.jakeer.transactionservice.kafka;

import com.jakeer.transactionservice.config.KafkaConsumerConfig;
import com.jakeer.transactionservice.dto.FraudResultEvent;
import com.jakeer.transactionservice.entity.TransactionStatus;
import com.jakeer.transactionservice.service.TransactionService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(
        classes = ProductionKafkaConsumerIntegrationTest.TestConfig.class
)
@EmbeddedKafka(
        partitions = 1,
        topics = {"fraud-result-topic", "fraud-result-topic.DLT"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class ProductionKafkaConsumerIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker broker;

    @Autowired
    TransactionService transactionService;

    @Autowired
    @Qualifier("dltKafkaTemplate")
    KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void productionConsumerShouldRetryAndPublishToDlt() throws Exception {

        doThrow(new RuntimeException("Simulated DB failure"))
                .when(transactionService)
                .updateTransactionStatus(
                        eq(99999L),
                        eq(TransactionStatus.DECLINED)
                );

        FraudResultEvent event = new FraudResultEvent();
        event.setTransactionId(99999L);
        event.setRiskScore(95);
        event.setDecision("DECLINE");
        event.setReason("Production consumer test");

        Map<String, Object> props = KafkaTestUtils.consumerProps(
                "production-dlt-verifier",
                "true",
                broker
        );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ByteArrayDeserializer.class
        );

        try (var consumer =
                     new DefaultKafkaConsumerFactory<String, byte[]>(props)
                             .createConsumer()) {

            consumer.subscribe(List.of("fraud-result-topic.DLT"));

            kafkaTemplate.send(
                    "fraud-result-topic",
                    "99999",
                    event
            ).get();

            ConsumerRecord<String, byte[]> record =
                    KafkaTestUtils.getSingleRecord(
                            consumer,
                            "fraud-result-topic.DLT",
                            Duration.ofSeconds(30)
                    );

            assertNotNull(record);
            assertEquals("99999", record.key());

            verify(transactionService, timeout(15000).times(4))
                    .updateTransactionStatus(
                            eq(99999L),
                            eq(TransactionStatus.DECLINED)
                    );
        }
    }

    @Configuration
    @EnableKafka
    @Import({
            KafkaConsumerConfig.class,
            FraudResultKafkaConsumer.class
    })
    static class TestConfig {

        @Bean
        TransactionService transactionService() {
            return mock(TransactionService.class);
        }

        @Bean(name = "dltKafkaTemplate")
        KafkaTemplate<String, Object> dltKafkaTemplate(
                EmbeddedKafkaBroker broker
        ) {
            Map<String, Object> props = new HashMap<>();

            props.put(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    broker.getBrokersAsString()
            );
            props.put(
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                    StringSerializer.class
            );
            props.put(
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                    JsonSerializer.class
            );

            return new KafkaTemplate<>(
                    new DefaultKafkaProducerFactory<>(props)
            );
        }
    }
}
