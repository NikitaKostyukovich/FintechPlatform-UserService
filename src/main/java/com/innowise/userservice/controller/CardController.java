package com.innowise.userservice.controller;

import com.innowise.userservice.dto.card.CardRequestDTO;
import com.innowise.userservice.dto.card.CardResponseDTO;
import com.innowise.userservice.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardResponseDTO> addCardToUser(
            @PathVariable Long userId,
            @RequestBody @Valid CardRequestDTO dto
    ) {
        CardResponseDTO createdCard = cardService.addCardToUser(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/users/{userId}/cards")
    public ResponseEntity<List<CardResponseDTO>> getCardsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @GetMapping("/cards/{id}")
    public ResponseEntity<CardResponseDTO> getCardById(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCardById(id));
    }

    @PutMapping("/cards/{id}")
    public ResponseEntity<CardResponseDTO> updateCard(
            @PathVariable Long id,
            @RequestBody @Valid CardRequestDTO dto
    ) {
        return ResponseEntity.ok(cardService.updateCard(id, dto));
    }

    @DeleteMapping("/cards/{id}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }
}