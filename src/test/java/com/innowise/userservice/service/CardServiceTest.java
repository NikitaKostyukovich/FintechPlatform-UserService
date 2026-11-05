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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CardMapper cardMapper;
    @Mock
    private CacheManager cacheManager;
    @InjectMocks
    private CardService cardService;

    private Jwt mockJwt(Long userId, String role) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("role")).thenReturn(role);
        if (!"ADMIN".equalsIgnoreCase(role)) {
            when(jwt.getClaim("user_id")).thenReturn(userId);
        }
        return jwt;
    }

    private User createUser(Long id) {
        User user = new User();
        user.setName("Test");
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (Exception ignored) {}
        return user;
    }


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
    void getCardById_asAdmin_returnsDto() {
        Card card = new Card();
        card.setUser(createUser(2L));
        CardResponseDTO response = mock(CardResponseDTO.class);
        Jwt adminJwt = mockJwt(1L, "ADMIN");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardMapper.toDTO(card)).thenReturn(response);

        assertSame(response, cardService.getCardById(10L, adminJwt));
    }

    @Test
    void getCardById_asOwner_returnsDto() {
        Card card = new Card();
        card.setUser(createUser(1L));
        CardResponseDTO response = mock(CardResponseDTO.class);
        Jwt userJwt = mockJwt(1L, "USER");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardMapper.toDTO(card)).thenReturn(response);

        assertSame(response, cardService.getCardById(10L, userJwt));
    }

    @Test
    void getCardById_asNotOwner_throwsAccessDenied() {
        Card card = new Card();
        card.setUser(createUser(1L));
        Jwt attackerJwt = mockJwt(2L, "USER");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.getCardById(10L, attackerJwt));
    }

    @Test
    void getCardById_notFound_throws() {
        Jwt jwt = mock(Jwt.class);
        when(cardRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getCardById(10L, jwt));
    }

    @Test
    void updateCard_asOwner_updatesAndSaves() {
        Card card = new Card();
        card.setUser(createUser(1L));
        CardRequestDTO dto = mock(CardRequestDTO.class);
        CardResponseDTO response = mock(CardResponseDTO.class);
        Jwt ownerJwt = mockJwt(1L, "USER");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(response);

        CardResponseDTO result = cardService.updateCard(10L, dto, ownerJwt);

        assertSame(response, result);
        verify(cardMapper).updateEntityFromDTO(dto, card);
        verify(cardRepository).save(card);
    }

    @Test
    void updateCard_asNotOwner_throwsAccessDenied() {
        Card card = new Card();
        card.setUser(createUser(1L));
        CardRequestDTO dto = mock(CardRequestDTO.class);
        Jwt attackerJwt = mockJwt(2L, "USER");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.updateCard(10L, dto, attackerJwt));
        verify(cardRepository, never()).save(any());
    }


    @Test
    void deleteCard_asAdmin_deletesSuccessfully() {
        Card card = new Card();
        card.setUser(createUser(1L));
        Jwt adminJwt = mockJwt(999L, "ADMIN");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        cardService.deleteCard(10L, adminJwt);

        verify(cardRepository).deleteById(10L);
    }

    @Test
    void deleteCard_asNotOwner_throwsAccessDenied() {
        Card card = new Card();
        card.setUser(createUser(1L));
        Jwt attackerJwt = mockJwt(2L, "USER");

        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.deleteCard(10L, attackerJwt));
        verify(cardRepository, never()).deleteById(any());
    }
    @Test
    void updateCard_asOwner_evictsUserCache() {
        // Arrange
        Long userId = 1L;
        Card card = new Card();
        card.setUser(createUser(userId));
        CardRequestDTO dto = mock(CardRequestDTO.class);
        Jwt ownerJwt = mockJwt(userId, "USER");
        Cache mockCache = mock(Cache.class);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(mock(CardResponseDTO.class));
        when(cacheManager.getCache("users")).thenReturn(mockCache);
        cardService.updateCard(10L, dto, ownerJwt);
        verify(cacheManager).getCache("users");
        verify(mockCache).evict(userId);
    }
    @Test
    void deleteCard_asAdmin_evictsUserCache() {
        Long userId = 42L;
        Card card = new Card();
        card.setUser(createUser(userId));
        Jwt adminJwt = mockJwt(999L, "ADMIN");
        Cache mockCache = mock(Cache.class);
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cacheManager.getCache("users")).thenReturn(mockCache);
        cardService.deleteCard(10L, adminJwt);
        verify(cacheManager).getCache("users");
        verify(mockCache).evict(userId);
    }
    @Test
    void updateCard_whenCacheManagerReturnsNullCache_doesNotThrowException() {
        Long userId = 1L;
        Card card = new Card();
        card.setUser(createUser(userId));
        CardRequestDTO dto = mock(CardRequestDTO.class);
        Jwt ownerJwt = mockJwt(userId, "USER");
        when(cardRepository.findById(10L)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDTO(card)).thenReturn(mock(CardResponseDTO.class));
        when(cacheManager.getCache("users")).thenReturn(null);
        cardService.updateCard(10L, dto, ownerJwt);
        verify(cacheManager).getCache("users");
    }
}