package com.jakeer.transactionservice.service;

import com.jakeer.transactionservice.entity.TransactionEntity;
import com.jakeer.transactionservice.entity.TransactionStatus;

import java.math.BigDecimal;

public interface TransactionService {
    TransactionEntity createTransaction(Long customerId,
                                        Long cardId,
                                        BigDecimal amount,
                                        String merchant,
                                        String location);



    void updateTransactionStatus(
            Long transactionId,
            TransactionStatus status
    );
}
