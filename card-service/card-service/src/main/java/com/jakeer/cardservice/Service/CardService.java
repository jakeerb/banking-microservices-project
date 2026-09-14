package com.jakeer.cardservice.Service;

import com.jakeer.cardservice.entity.CardEntity;

public interface CardService {
  CardEntity createCard(Long customerId, String cardType);

}
