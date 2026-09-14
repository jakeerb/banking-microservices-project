package com.jakeer.frauddetection_.service;

import com.jakeer.frauddetection_.dto.FraudResultEvent;
import com.jakeer.frauddetection_.entity.FraudDecision;
import com.jakeer.frauddetection_.entity.FraudDetectionEntity;
import com.jakeer.frauddetection_.kafka.FraudResultKafkaProducer;
import com.jakeer.frauddetection_.repository.FraudDetectionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class FraudDetectionServiceImpl implements FraudDetectionService {

    private final FraudDetectionRepository fraudDetectionRepository;
    private final FraudResultKafkaProducer fraudResultKafkaProducer;

    public FraudDetectionServiceImpl(
            FraudDetectionRepository fraudDetectionRepository,
            FraudResultKafkaProducer fraudResultKafkaProducer) {

        this.fraudDetectionRepository = fraudDetectionRepository;
        this.fraudResultKafkaProducer = fraudResultKafkaProducer;
    }

    @Override
    public FraudDetectionEntity analyzeTransaction(
            Long transactionId,
            Long customerId,
            Long cardId,
            BigDecimal amount,
            String merchant,
            String location) {

        int riskScore = 0;
        StringBuilder reason = new StringBuilder();

        // Rule 1: High transaction amount
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            riskScore += 30;
            reason.append("High transaction amount; ");
        }

        // Rule 2: Suspicious merchant
        if (merchant != null &&
                (merchant.equalsIgnoreCase("Unknown")
                        || merchant.equalsIgnoreCase("Suspicious Merchant"))) {

            riskScore += 15;
            reason.append("Suspicious merchant; ");
        }

        // Rule 3: Unknown location
        if (location != null && location.equalsIgnoreCase("Unknown")) {
            riskScore += 25;
            reason.append("Unknown transaction location; ");
        }

        // Final decision
        FraudDecision decision;

        if (riskScore <= 30) {
            decision = FraudDecision.APPROVE;
        } else if (riskScore <= 60) {
            decision = FraudDecision.REVIEW;
        } else {
            decision = FraudDecision.DECLINE;
        }

        // Create fraud detection result
        FraudDetectionEntity fraud = new FraudDetectionEntity();

        fraud.setTransactionId(transactionId);
        fraud.setCustomerId(customerId);
        fraud.setCardId(cardId);
        fraud.setAmount(amount);
        fraud.setMerchant(merchant);
        fraud.setLocation(location);
        fraud.setRiskScore(riskScore);
        fraud.setDecision(decision);
        fraud.setReason(
                reason.length() == 0
                        ? "No suspicious activity detected"
                        : reason.toString()
        );
        fraud.setDetectedAt(LocalDateTime.now());

        // Save fraud result into database
        FraudDetectionEntity savedFraud =
                fraudDetectionRepository.save(fraud);

        System.out.println(
                "Fraud Decision: " + savedFraud.getDecision()
                        + " | Risk Score: " + savedFraud.getRiskScore()
        );

        // Create Kafka result event
        FraudResultEvent result = new FraudResultEvent();

        result.setTransactionId(savedFraud.getTransactionId());
        result.setRiskScore(savedFraud.getRiskScore());
        result.setDecision(savedFraud.getDecision().name());
        result.setReason(savedFraud.getReason());

        // Publish result to fraud-result-topic
        fraudResultKafkaProducer.sendFraudResult(result);

        return savedFraud;
    }
}