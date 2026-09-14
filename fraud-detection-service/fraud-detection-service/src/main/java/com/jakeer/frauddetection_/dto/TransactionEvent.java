package com.jakeer.frauddetection_.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionEvent {

    private Long id;
    private Long customerId;
    private Long cardId;
    private BigDecimal amount;
    private String merchant;
    private String location;
    private String status;
    private LocalDateTime transactionTime;
    private LocalDateTime createdAt;
}