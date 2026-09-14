package com.jakeer.transactionservice.kafka;

import com.jakeer.transactionservice.dto.FraudResultEvent;
import com.jakeer.transactionservice.entity.TransactionStatus;
import com.jakeer.transactionservice.service.TransactionService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FraudResultKafkaConsumer {

    private final TransactionService transactionService;

    public FraudResultKafkaConsumer(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @KafkaListener(
            topics = "fraud-result-topic",
            groupId = "transaction-service-group"
    )
    public void consumeFraudResult(FraudResultEvent result) {

        System.out.println(
                "Received fraud result for transaction: "
                        + result.getTransactionId()
                        + " Decision: "
                        + result.getDecision()
        );

        TransactionStatus status;

        switch (result.getDecision().toUpperCase()) {
            case "APPROVE":
                status = TransactionStatus.APPROVED;
                break;

            case "REVIEW":
                status = TransactionStatus.REVIEW;
                break;

            case "DECLINE":
                status = TransactionStatus.DECLINED;
                break;

            default:
                throw new IllegalArgumentException(
                        "Unknown fraud decision: " + result.getDecision()
                );
        }

        transactionService.updateTransactionStatus(
                result.getTransactionId(),
                status
        );

        System.out.println(
                "Transaction status updated to: " + status
        );
    }
}