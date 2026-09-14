package com.jakeer.frauddetection_.service;

import com.jakeer.frauddetection_.entity.FraudDetectionEntity;

import java.math.BigDecimal;

public interface FraudDetectionService {
    FraudDetectionEntity analyzeTransaction(
            Long transactionId,
            Long customerId,
            Long cardId,
            BigDecimal amount,
            String merchant,
            String location
    );



}
