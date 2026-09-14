package com.jakeer.frauddetection_.repository;

import com.jakeer.frauddetection_.entity.FraudDetectionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudDetectionRepository  extends JpaRepository<FraudDetectionEntity,Long> {

}
