package com.jakeer.cardservice.Service;

import com.jakeer.cardservice.entity.CardEntity;
import com.jakeer.cardservice.entity.CardStatus;
import com.jakeer.cardservice.entity.CardType;
import com.jakeer.cardservice.repository.CardRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CardServiceimpl implements CardService {

    private final CardRepository cardRepository;

    public CardServiceimpl(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    @Override
    public CardEntity createCard(Long customerId, String cardType) {

        CardEntity card = new CardEntity();

        card.setCustomerId(customerId);

        card.setCardType(
                CardType.valueOf(cardType.toUpperCase())
        );

        card.setCardStatus(CardStatus.ACTIVE);

        card.setExpiryDate(
                LocalDate.now().plusYears(5)
        );

        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());

        return cardRepository.save(card);
    }
}