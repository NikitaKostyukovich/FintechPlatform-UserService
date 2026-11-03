package com.innowise.userservice.service;

import com.innowise.userservice.dto.card.CardRequestDTO;
import com.innowise.userservice.dto.card.CardResponseDTO;
import com.innowise.userservice.entity.Card;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.CardLimitExceededException;
import com.innowise.userservice.exception.CardNotFoundException;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.CardMapper;
import com.innowise.userservice.repository.CardRepository;
import com.innowise.userservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardMapper cardMapper;
    private final CacheManager cacheManager;

    public CardService(CardRepository cardRepository, UserRepository userRepository, CardMapper cardMapper, CacheManager cacheManager) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.cardMapper = cardMapper;
        this.cacheManager = cacheManager;
    }
    @CacheEvict(value = "users", key = "#userId")
    @Transactional
    public CardResponseDTO addCardToUser(Long userId, CardRequestDTO dto) {
        log.info("Attempting to add new card for user with id: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        long activeCardsCount = cardRepository.countByUserIdAndActiveTrue(userId);
        if (activeCardsCount >= 5) {
            log.warn("User with id: {} has reached the card limit", userId);
            throw new CardLimitExceededException("User has reached the maximum limit of 5 active cards.");
        }

        Card card = cardMapper.toEntity(dto);
        card.setUser(user);

        Card savedCard = cardRepository.save(card);
        log.info("Successfully added card with id: {} to user with id: {}", savedCard.getId(), userId);

        return cardMapper.toDTO(savedCard);
    }

    public List<CardResponseDTO> getCardsByUserId(Long userId) {
        log.info("Fetching cards for user with id: {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException("User not found with id: " + userId);
        }

        List<Card> cards = cardRepository.findByUserId(userId);
        log.info("Retrieved {} cards for user with id: {}", cards.size(), userId);

        return cards.stream()
                .map(cardMapper::toDTO)
                .collect(Collectors.toList());
    }

    public CardResponseDTO getCardById(Long cardId) {
        log.info("Fetching card with id: {}", cardId);

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));
        return cardMapper.toDTO(card);
    }
    @Transactional
    public CardResponseDTO updateCard(Long cardId, CardRequestDTO dto) {
        log.info("Attempting to update card with id: {}", cardId);

        Card existingCard = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found with id: " + cardId));

        cardMapper.updateEntityFromDTO(dto, existingCard);

        Card updatedCard = cardRepository.save(existingCard);
        if (existingCard.getUser() != null) {
            evictUserCache(existingCard.getUser().getId());
        }        log.info("Successfully updated card with id: {}", cardId);
        return cardMapper.toDTO(updatedCard);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        log.info("Attempting to deactivate card with id: {}", cardId);
        Card existingCard = cardRepository.findById(cardId).orElseThrow(()->new CardNotFoundException("Card not found with id: " + cardId));

        cardRepository.deleteById(cardId);

        if (existingCard.getUser() != null) {
            evictUserCache(existingCard.getUser().getId());
        }
        log.info("Successfully deactivated card with id: {}", cardId);
    }
    private void evictUserCache(Long userId){
        Cache userCache = cacheManager.getCache("users");
        if(userCache != null){
            userCache.evict(userId);
        }
    }
}