package com.jakeer.transactionservice.repository;

import com.jakeer.transactionservice.entity.TransactionEntity;
import com.jakeer.transactionservice.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    // Existing query - keep it
    @Query("SELECT COUNT(t) FROM TransactionEntity t")
    Long countTotalTransactions();

    // Dashboard - total transactions
    @Query("""
        SELECT COUNT(t)
        FROM TransactionEntity t
        WHERE (:from IS NULL OR t.transactionTime >= :from)
          AND (:to IS NULL OR t.transactionTime < :to)
    """)
    Long countTransactions(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Dashboard - count by status
    @Query("""
        SELECT COUNT(t)
        FROM TransactionEntity t
        WHERE t.status = :status
          AND (:from IS NULL OR t.transactionTime >= :from)
          AND (:to IS NULL OR t.transactionTime < :to)
    """)
    Long countTransactionsByStatus(
            @Param("status") TransactionStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Dashboard - total transaction amount
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0)
        FROM TransactionEntity t
        WHERE (:from IS NULL OR t.transactionTime >= :from)
          AND (:to IS NULL OR t.transactionTime < :to)
    """)
    BigDecimal sumTransactionAmount(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    // Dashboard - average transaction amount
    @Query("""
        SELECT COALESCE(AVG(t.amount), 0)
        FROM TransactionEntity t
        WHERE (:from IS NULL OR t.transactionTime >= :from)
          AND (:to IS NULL OR t.transactionTime < :to)
    """)
    BigDecimal averageTransactionAmount(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}