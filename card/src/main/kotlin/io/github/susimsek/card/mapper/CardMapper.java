package io.github.susimsek.card.mapper;

import io.github.susimsek.card.dto.CardDTO;
import io.github.susimsek.card.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper extends EntityMapper<CardDTO, Card> {
}
