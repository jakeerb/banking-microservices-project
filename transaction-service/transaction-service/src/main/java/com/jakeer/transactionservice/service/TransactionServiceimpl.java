package com.jakeer.transactionservice.service;

import com.jakeer.transactionservice.entity.TransactionEntity;
import com.jakeer.transactionservice.entity.TransactionStatus;
import com.jakeer.transactionservice.kafka.TransactionKafkaProducer;
import com.jakeer.transactionservice.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionServiceimpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionKafkaProducer transactionKafkaProducer;

    public TransactionServiceimpl(
            TransactionRepository transactionRepository,
            TransactionKafkaProducer transactionKafkaProducer) {

        this.transactionRepository = transactionRepository;
        this.transactionKafkaProducer = transactionKafkaProducer;
    }

    @Override
    public TransactionEntity createTransaction(
            Long customerId,
            Long cardId,
            BigDecimal amount,
            String merchant,
            String location) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setCustomerId(customerId);
        transaction.setCardId(cardId);
        transaction.setAmount(amount);
        transaction.setMerchant(merchant);
        transaction.setLocation(location);

        transaction.setStatus(TransactionStatus.PENDING);

        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setCreatedAt(LocalDateTime.now());

        // Save transaction in database
        TransactionEntity savedTransaction =
                transactionRepository.save(transaction);

        // Publish transaction event to Kafka
        transactionKafkaProducer.sendTransaction(savedTransaction);

        return savedTransaction;
    }

    @Override
    public void updateTransactionStatus(
            Long transactionId,
            TransactionStatus status) {

        TransactionEntity transaction =
                transactionRepository.findById(transactionId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Transaction not found: " + transactionId));

        transaction.setStatus(status);

        transactionRepository.save(transaction);
    }
}