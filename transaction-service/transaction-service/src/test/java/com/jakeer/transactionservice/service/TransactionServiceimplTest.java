package com.jakeer.transactionservice.service;

import com.jakeer.transactionservice.entity.TransactionEntity;
import com.jakeer.transactionservice.entity.TransactionStatus;
import com.jakeer.transactionservice.kafka.TransactionKafkaProducer;
import com.jakeer.transactionservice.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceimplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionKafkaProducer transactionKafkaProducer;

    @InjectMocks
    private TransactionServiceimpl transactionService;

    @Test
    void createTransaction_setsFieldsSavesAndPublishesTransaction() {
        Long customerId = 42L;
        Long cardId = 7L;
        BigDecimal amount = new BigDecimal("125.50");
        String merchant = "Corner Shop";
        String location = "London";
        LocalDateTime beforeCreation = LocalDateTime.now();

        when(transactionRepository.save(any(TransactionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionEntity result = transactionService.createTransaction(
                customerId, cardId, amount, merchant, location);

        LocalDateTime afterCreation = LocalDateTime.now();
        ArgumentCaptor<TransactionEntity> transactionCaptor =
                ArgumentCaptor.forClass(TransactionEntity.class);
        verify(transactionRepository).save(transactionCaptor.capture());

        TransactionEntity savedTransaction = transactionCaptor.getValue();
        assertSame(savedTransaction, result);
        assertEquals(customerId, savedTransaction.getCustomerId());
        assertEquals(cardId, savedTransaction.getCardId());
        assertEquals(amount, savedTransaction.getAmount());
        assertEquals(merchant, savedTransaction.getMerchant());
        assertEquals(location, savedTransaction.getLocation());
        assertEquals(TransactionStatus.PENDING, savedTransaction.getStatus());

        assertNotNull(savedTransaction.getTransactionTime());
        assertFalse(savedTransaction.getTransactionTime().isBefore(beforeCreation));
        assertFalse(savedTransaction.getTransactionTime().isAfter(afterCreation));
        assertNotNull(savedTransaction.getCreatedAt());
        assertFalse(savedTransaction.getCreatedAt().isBefore(beforeCreation));
        assertFalse(savedTransaction.getCreatedAt().isAfter(afterCreation));

        verify(transactionKafkaProducer).sendTransaction(savedTransaction);
    }

    @Test
    void updateTransactionStatus_updatesAndSavesTransaction() {
        Long transactionId = 15L;
        TransactionEntity transaction = new TransactionEntity();
        transaction.setStatus(TransactionStatus.PENDING);
        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.of(transaction));

        transactionService.updateTransactionStatus(
                transactionId, TransactionStatus.APPROVED);

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void updateTransactionStatus_whenTransactionIsMissing_throwsAndDoesNotSave() {
        Long transactionId = 15L;
        when(transactionRepository.findById(transactionId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transactionService.updateTransactionStatus(
                        transactionId, TransactionStatus.APPROVED));

        assertEquals("Transaction not found: " + transactionId, exception.getMessage());
        verify(transactionRepository, never()).save(any(TransactionEntity.class));
    }

    @Test
    void getTotalTransactions_returnsRepositoryCount() {
        Long totalTransactions = 27L;
        when(transactionRepository.countTotalTransactions())
                .thenReturn(totalTransactions);

        assertEquals(totalTransactions, transactionService.getTotalTransactions());
        verify(transactionRepository).countTotalTransactions();
    }
}
