package com.jakeer.frauddetection_.dto;

import lombok.Data;

@Data
public class FraudResultEvent {

    private Long transactionId;

    private Integer riskScore;

    private String decision;

    private String reason;
}