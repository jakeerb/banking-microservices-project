package com.jakeer.frauddetection_.kafka;

import com.jakeer.frauddetection_.dto.TransactionEvent;
import com.jakeer.frauddetection_.entity.FraudDetectionEntity;
import com.jakeer.frauddetection_.service.FraudDetectionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FraudDetectionKafkaConsumer {

    private final FraudDetectionService fraudDetectionService;

    public FraudDetectionKafkaConsumer(
            FraudDetectionService fraudDetectionService) {
        this.fraudDetectionService = fraudDetectionService;
    }

    @KafkaListener(
            topics = "transaction-topic",
            groupId = "fraud-detection-group"
    )
    public void consumeTransaction(TransactionEvent transactionEvent) {

        System.out.println(
                "Received transaction: "
                        + transactionEvent.getId()
        );

        FraudDetectionEntity result =
                fraudDetectionService.analyzeTransaction(
                        transactionEvent.getId(),
                        transactionEvent.getCustomerId(),
                        transactionEvent.getCardId(),
                        transactionEvent.getAmount(),
                        transactionEvent.getMerchant(),
                        transactionEvent.getLocation()
                );

        System.out.println(
                "Fraud Decision: "
                        + result.getDecision()
                        + " | Risk Score: "
                        + result.getRiskScore()
        );
    }
}