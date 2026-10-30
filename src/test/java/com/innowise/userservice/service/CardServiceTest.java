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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @InjectMocks
    private CardService cardService;

    @Test
    void addCardToUser_underLimit_savesCardWithOwner() {
        User user = new User();
        Card card = new Card();
        Card saved = new Card();
        CardRequestDTO dto = mock(CardRequestDTO.class);
        CardResponseDTO response = mock(CardResponseDTO.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardRepository.countByUserIdAndActiveTrue(1L)).thenReturn(4L);
        when(cardMapper.toEntity(dto)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(saved);
        when(cardMapper.toDTO(saved)).thenReturn(response);

        CardResponseDTO result = cardService.addCardToUser(1L, dto);

        assertSame(response, result);
        assertSame(user, card.getUser());
        verify(cardRepository).save(card);
    }

    @Test
    void addCardToUser_limitReached_throwsAndDoesNotSave() {
        CardRequestDTO dto = mock(CardRequestDTO.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));
        when(cardRepository.countByUserIdAndActiveTrue(1L)).thenReturn(5L);

        assertThrows(CardLimitExceededException.class, () -> cardService.addCardToUser(1L, dto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void addCardToUser_userNotFound_throws() {
        CardRequestDTO dto = mock(CardRequestDTO.class);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.addCardToUser(1L, dto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void getCardsByUserId_userExists_returnsMappedList() {
        Card card = new Card();
        CardResponseDTO response = mock(CardResponseDTO.class);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findByUserId(1L)).thenReturn(List.of(card));
        when(cardMapper.toDTO(card)).thenReturn(response);

        List<CardResponseDTO> result = cardService.getCardsByUserId(1L);

        assertEquals(1, result.size());
        assertSame(response, result.get(0));
    }

    @Test
    void getCardsByUserId_userNotFound_throws() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> cardService.getCardsByUserId(1L));
        verify(cardRepository, never()).findByUserId(any());
    }

    @Test
    void getCardById_found_returnsDto() {
        Card card = new Card();
        CardResponseDTO response = mock(CardResponseDTO.class);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardMapper.toDTO(card)).thenReturn(response);

        assertSame(response, cardService.getCardById(1L));
    }

    @Test
    void getCardById_notFound_throws() {
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(1L));
    }

    @Test
    void updateCard_found_updatesEntityAndSaves() {
        Card card = new Card();
        CardRequestDTO dto = mock(CardRequestDTO.class);
        CardResponseDTO response = mock(CardResponseDTO.class);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(response);

        CardResponseDTO result = cardService.updateCard(1L, dto);

        assertSame(response, result);
        verify(cardMapper).updateEntityFromDTO(dto, card);
        verify(cardRepository).save(card);
    }

    @Test
    void updateCard_notFound_throwsAndDoesNotSave() {
        CardRequestDTO dto = mock(CardRequestDTO.class);
        when(cardRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.updateCard(1L, dto));
        verify(cardRepository, never()).save(any());
    }

    @Test
    void deleteCard_exists_deactivatesCard() {
        when(cardRepository.existsById(1L)).thenReturn(true);

        cardService.deleteCard(1L);

        verify(cardRepository).activateOrDeactivateCard(false, 1L);
    }

    @Test
    void deleteCard_notFound_throws() {
        when(cardRepository.existsById(1L)).thenReturn(false);

        assertThrows(CardNotFoundException.class, () -> cardService.deleteCard(1L));
        verify(cardRepository, never()).activateOrDeactivateCard(anyBoolean(), any());
    }
}
