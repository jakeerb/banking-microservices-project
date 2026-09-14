package com.jakeer.transactionservice.kafka;

import com.jakeer.transactionservice.entity.TransactionEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TransactionKafkaProducer {

    private final KafkaTemplate<String, TransactionEntity> kafkaTemplate;

    public TransactionKafkaProducer(
            KafkaTemplate<String, TransactionEntity> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendTransaction(TransactionEntity transaction) {

        kafkaTemplate.send(
                "transaction-topic",
                transaction.getId().toString(),
                transaction
        );
    }
}