package com.innowise.userservice.service;

import com.innowise.userservice.dto.user.UserRequestDTO;
import com.innowise.userservice.dto.user.UserResponseDTO;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @InjectMocks
    private UserService userService;

    @Test
    void createUser_savesEntityAndReturnsDto() {
        UserRequestDTO dto = mock(UserRequestDTO.class);
        UserResponseDTO response = mock(UserResponseDTO.class);
        User entity = new User();
        User saved = new User();

        when(userMapper.toEntity(dto)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(saved);
        when(userMapper.toDTO(saved)).thenReturn(response);

        UserResponseDTO result = userService.createUser(dto);

        assertSame(response, result);
        verify(userRepository).save(entity);
    }

    @Test
    void getUserById_found_returnsDto() {
        User user = new User();
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(response);

        assertSame(response, userService.getUserById(1L));
    }

    @Test
    void getUserById_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
        verify(userMapper, never()).toDTO(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getFilteredUsers_mapsEveryUserOnThePage() {
        String name = "Ivan";
        String surname = "Petrov";
        Pageable pageable = PageRequest.of(0, 10);
        User user = new User();
        UserResponseDTO response = mock(UserResponseDTO.class);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(user), pageable, 1));
        when(userMapper.toDTO(user)).thenReturn(response);

        Page<UserResponseDTO> result = userService.getFilteredUsers(name, surname, pageable);

        assertEquals(1, result.getTotalElements());
        assertSame(response, result.getContent().get(0));
    }

    @Test
    void updateUser_found_updatesEntityAndSaves() {
        UserRequestDTO dto = mock(UserRequestDTO.class);
        UserResponseDTO response = mock(UserResponseDTO.class);
        User user = new User();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(response);

        UserResponseDTO result = userService.updateUser(1L, dto);

        assertSame(response, result);
        verify(userMapper).updateUserFromDto(dto, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_notFound_throwsAndDoesNotSave() {
        UserRequestDTO dto = mock(UserRequestDTO.class);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_exists_deletes() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_throwsAndDoesNotDelete() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void updateUserStatus_exists_callsModifyingQuery() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.updateUserStatus(1L, false);

        verify(userRepository).updateActivityAndTime(false, 1L);
    }

    @Test
    void updateUserStatus_notFound_throws() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.updateUserStatus(1L, true));
        verify(userRepository, never()).updateActivityAndTime(anyBoolean(), any());
    }
}