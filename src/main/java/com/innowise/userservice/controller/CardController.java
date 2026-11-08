package com.innowise.userservice.controller;

import com.innowise.userservice.dto.card.CardRequestDTO;
import com.innowise.userservice.dto.card.CardResponseDTO;
import com.innowise.userservice.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponseDTO> addCardToUser(
            @PathVariable Long userId,
            @RequestBody @Valid CardRequestDTO dto
    ) {
        CardResponseDTO createdCard = cardService.addCardToUser(userId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @PostMapping("/cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardResponseDTO> addCard(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CardRequestDTO dto){
        Long targetUserID = ((Number) jwt.getClaim("user_id")).longValue();
        CardResponseDTO createdCard = cardService.addCardToUser(targetUserID, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @GetMapping("/users/{userId}/cards")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardResponseDTO>> getCardsByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCardsByUserId(userId));
    }

    @GetMapping("/cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CardResponseDTO>> getCards(@AuthenticationPrincipal Jwt jwt){
        Long currentUserId = ((Number) jwt.getClaim("user_id")).longValue();
        return ResponseEntity.ok(cardService.getCardsByUserId(currentUserId));
    }

    @GetMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<CardResponseDTO> getCardById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(cardService.getCardById(id, jwt));
    }

    @PutMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<CardResponseDTO> updateCard(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody @Valid CardRequestDTO dto
    ) {
        return ResponseEntity.ok(cardService.updateCard(id, dto, jwt));
    }
    @DeleteMapping("/cards/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Void> deleteCard(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        cardService.deleteCard(id, jwt);
        return ResponseEntity.noContent().build();
    }
}