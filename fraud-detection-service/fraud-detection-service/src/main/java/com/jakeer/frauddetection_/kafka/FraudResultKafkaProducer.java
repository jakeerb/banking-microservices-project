package com.jakeer.frauddetection_.kafka;

import com.jakeer.frauddetection_.dto.FraudResultEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FraudResultKafkaProducer {

    private final KafkaTemplate<String, FraudResultEvent> kafkaTemplate;

    public FraudResultKafkaProducer(
            KafkaTemplate<String, FraudResultEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendFraudResult(FraudResultEvent result) {

        kafkaTemplate.send(
                "fraud-result-topic",
                result.getTransactionId().toString(),
                result
        );
    }
}