package com.innowise.userservice.mapper;

import com.innowise.userservice.dto.card.CardRequestDTO;
import com.innowise.userservice.dto.card.CardResponseDTO;
import com.innowise.userservice.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CardMapper {

    Card toEntity(CardRequestDTO dto);

    @Mapping(source = "user.id", target = "userId")
    CardResponseDTO toDTO(Card entity);

    void updateEntityFromDTO(CardRequestDTO dto, @MappingTarget Card card);
}
