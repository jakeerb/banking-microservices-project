package com.jakeer.transactionservice.repository;

import com.jakeer.transactionservice.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity,Long> {

}
