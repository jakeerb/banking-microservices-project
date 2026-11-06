
package com.jakeer.transactionservice.kafka;

import com.jakeer.transactionservice.config.KafkaConsumerConfig;
import com.jakeer.transactionservice.dto.FraudResultEvent;
import com.jakeer.transactionservice.service.TransactionService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringJUnitConfig(classes = KafkaRetryDltIntegrationTest.TestConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {"fraud-result-test", "fraud-result-test.DLT"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class KafkaRetryDltIntegrationTest {

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    EmbeddedKafkaBroker broker;

    @Autowired
    TransactionService transactionService;

    @Test
    void failedFraudResultShouldReachDlt() throws Exception {

        doThrow(new RuntimeException("Simulated DB failure"))
                .when(transactionService)
                .updateTransactionStatus(eq(99999L), any());

        FraudResultEvent event = new FraudResultEvent();
        event.setTransactionId(99999L);
        event.setRiskScore(95);
        event.setDecision("DECLINE");
        event.setReason("Integration test");

        Map<String, Object> props =
                KafkaTestUtils.consumerProps(
                        "dlt-test-group",
                        "true",
                        broker
                );

        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.ByteArrayDeserializer.class
        );

        try (var consumer =
                     new DefaultKafkaConsumerFactory<String, byte[]>(props)
                             .createConsumer()) {

            consumer.subscribe(
                    java.util.List.of("fraud-result-test.DLT")
            );

            kafkaTemplate.send(
                    "fraud-result-test",
                    "99999",
                    event
            ).get();

            ConsumerRecord<String, byte[]> record =
                    KafkaTestUtils.getSingleRecord(
                            consumer,
                            "fraud-result-test.DLT",
                            Duration.ofSeconds(30)
                    );

            assertNotNull(record);
            assertEquals("99999", record.key());

            verify(transactionService, timeout(15000).times(4))
                    .updateTransactionStatus(eq(99999L), any());
        }
    }

    @Configuration
    @EnableKafka
    static class TestConfig {

        @Bean
        TransactionService transactionService() {
            return mock(TransactionService.class);
        }

        @Bean
        KafkaTemplate<String, Object> kafkaTemplate(
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

        @Bean
        ConsumerFactory<String, FraudResultEvent> consumerFactory(
                EmbeddedKafkaBroker broker
        ) {
            Map<String, Object> props = new HashMap<>();
            props.put(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    broker.getBrokersAsString()
            );
            props.put(
                    ConsumerConfig.GROUP_ID_CONFIG,
                    "retry-test-group"
            );
            props.put(
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                    "earliest"
            );
            props.put(
                    org.springframework.kafka.support.serializer.JsonDeserializer
                            .TRUSTED_PACKAGES,
                    "com.jakeer.transactionservice.dto"
            );
            props.put(
                    org.springframework.kafka.support.serializer.JsonDeserializer
                            .VALUE_DEFAULT_TYPE,
                    FraudResultEvent.class.getName()
            );

            return new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    new org.springframework.kafka.support.serializer
                            .JsonDeserializer<>(FraudResultEvent.class)
            );
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, FraudResultEvent>
        kafkaListenerContainerFactory(
                ConsumerFactory<String, FraudResultEvent> consumerFactory,
                KafkaTemplate<String, Object> kafkaTemplate
        ) {
            var factory =
                    new ConcurrentKafkaListenerContainerFactory
                            <String, FraudResultEvent>();

            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(
                    ContainerProperties.AckMode.RECORD
            );

            var recoverer = new DeadLetterPublishingRecoverer(
                    kafkaTemplate,
                    (record, exception) ->
                            new TopicPartition(
                                    record.topic() + ".DLT",
                                    record.partition()
                            )
            );

            factory.setCommonErrorHandler(
                    new DefaultErrorHandler(
                            recoverer,
                            new FixedBackOff(2000L, 3L)
                    )
            );

            return factory;
        }

        @Bean
        TestListener testListener(TransactionService service) {
            return new TestListener(service);
        }
    }

    static class TestListener {

        private final TransactionService service;

        TestListener(TransactionService service) {
            this.service = service;
        }

        @KafkaListener(
                topics = "fraud-result-test",
                groupId = "retry-test-group"
        )
        public void consume(FraudResultEvent event) {
            service.updateTransactionStatus(
                    event.getTransactionId(),
                    com.jakeer.transactionservice.entity
                            .TransactionStatus.DECLINED
            );
        }
    }
}
