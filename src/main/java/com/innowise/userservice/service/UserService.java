package com.innowise.userservice.service;

import com.innowise.userservice.dto.user.UserRequestDTO;
import com.innowise.userservice.dto.user.UserResponseDTO;
import com.innowise.userservice.entity.User;
import com.innowise.userservice.exception.UserNotFoundException;
import com.innowise.userservice.mapper.UserMapper;
import com.innowise.userservice.repository.UserRepository;
import com.innowise.userservice.specification.UserSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO dto) {
        log.info("Creating new user with email: {}", dto.email());
        User user = userMapper.toEntity(dto);
        User savedUser = userRepository.save(user);
        log.info("User created successfully with id: {}", savedUser.getId());
        return userMapper.toDTO(savedUser);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserResponseDTO getUserById(Long id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found with id: {}", id);
                    return new UserNotFoundException("There is no user with such id");
                });
        return userMapper.toDTO(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponseDTO> getFilteredUsers(String name, String surname, Pageable pageable) {
        log.info("Fetching filtered users. Name: {}, Surname: {}", name, surname);
        Specification<User> spec = Specification.where(UserSpecification.findByName(name))
                .and(UserSpecification.findBySurname(surname));
        Page<User> foundedUsers = userRepository.findAll(spec, pageable);
        return foundedUsers.map(userMapper::toDTO);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public UserResponseDTO updateUser(Long id, UserRequestDTO dto) {
        log.info("Updating user with id: {}", id);
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Cannot update. User not found with id: {}", id);
                    return new UserNotFoundException("Cannot update. User not found with id: " + id);
                });
        userMapper.updateUserFromDto(dto, existingUser);
        User updatedUser = userRepository.save(existingUser);
        log.info("User with id: {} updated successfully", id);
        return userMapper.toDTO(updatedUser);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);
        if (!userRepository.existsById(id)) {
            log.error("Cannot delete. User not found with id: {}", id);
            throw new UserNotFoundException("Cannot delete. User not found with id: " + id);
        }
        userRepository.deleteById(id);
        log.info("User with id: {} deleted successfully", id);
    }

    @Transactional
    @CacheEvict(value = "users", key = "#id")
    public void updateUserStatus(Long id, boolean active) {
        log.info("Updating status for user id: {} to active: {}", id, active);
        if (userRepository.existsById(id)) {
            userRepository.updateActivityAndTime(active, id);
            log.info("Status updated successfully for user id: {}", id);
        } else {
            log.error("Cannot update status. User not found with id: {}", id);
            throw new UserNotFoundException("User not found with id:" + id);
        }
    }
}