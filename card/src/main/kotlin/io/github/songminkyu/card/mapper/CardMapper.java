package io.github.songminkyu.card.mapper;

import io.github.songminkyu.card.dto.CardDTO;
import io.github.songminkyu.card.entity.Card;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardMapper extends EntityMapper<CardDTO, Card> {
}
