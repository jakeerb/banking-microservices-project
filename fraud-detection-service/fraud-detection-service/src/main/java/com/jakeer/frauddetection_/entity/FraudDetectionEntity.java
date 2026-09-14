package com.jakeer.frauddetection_.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_detection")
@Data
public class FraudDetectionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long transactionId;

    private Long customerId;

    private Long cardId;

    private BigDecimal amount;

    private String merchant;

    private String location;

    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    private FraudDecision decision;

    private String reason;

    private LocalDateTime detectedAt;
}