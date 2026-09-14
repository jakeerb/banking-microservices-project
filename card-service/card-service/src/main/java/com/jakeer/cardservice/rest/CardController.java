package com.jakeer.cardservice.rest;


import com.jakeer.cardservice.Service.CardService;
import com.jakeer.cardservice.entity.CardEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    public ResponseEntity<CardEntity> createCard(
            @RequestParam Long customerId,
            @RequestParam String cardType) {

        CardEntity card =
                cardService.createCard(customerId, cardType);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(card);
    }

}
