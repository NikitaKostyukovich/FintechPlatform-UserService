package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.user.UserRequestDTO;
import com.innowise.userservice.dto.user.UserResponseDTO;
import com.innowise.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CardMapper.class)
public interface UserMapper {
    UserResponseDTO toDTO(User entity);
    User toEntity(UserRequestDTO dto);
    void updateUserFromDto(UserRequestDTO dto, @MappingTarget  User user);
}
