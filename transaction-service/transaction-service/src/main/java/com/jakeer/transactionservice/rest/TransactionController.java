package com.jakeer.transactionservice.rest;

import com.jakeer.transactionservice.entity.TransactionEntity;
import com.jakeer.transactionservice.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionEntity> createTransaction(
            @RequestParam Long customerId,
            @RequestParam Long cardId,
            @RequestParam BigDecimal amount,
            @RequestParam String merchant,
            @RequestParam String location) {

        TransactionEntity transaction =
                transactionService.createTransaction(
                        customerId,
                        cardId,
                        amount,
                        merchant,
                        location);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(transaction);
    }
}