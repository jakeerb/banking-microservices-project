package com.jakeer.cardservice.repository;

import com.jakeer.cardservice.entity.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<CardEntity,Long> {

}
