package com.jakeer.transactionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private Long totalTransactions;

    private Long pendingTransactions;
    private Long approvedTransactions;
    private Long reviewTransactions;
    private Long declinedTransactions;

    private BigDecimal totalTransactionAmount;
    private BigDecimal averageTransactionAmount;

    private LocalDate from;
    private LocalDate to;
}