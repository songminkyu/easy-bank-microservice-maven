package io.github.songminkyu.card.controller;

import io.github.songminkyu.card.dto.CardDTO;
import io.github.songminkyu.card.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.security.access.prepost.PreAuthorize;

@Slf4j
@GraphQLController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority(T(io.github.songminkyu.card.security.AuthoritiesConstants).CARD)")
public class CardGraphQLController {

    private final CardService cardService;

    @QueryMapping
    Window<CardDTO> cards(ScrollSubrange subrange) {
        return cardService.getAllCards(subrange);
    }

    @QueryMapping
    public CardDTO card(@Argument String mobileNumber) {
        return cardService.getCard(mobileNumber);
    }

    @MutationMapping
    public CardDTO createCard(@Argument String mobileNumber) {
        return cardService.createCard(mobileNumber);
    }

    @MutationMapping
    public String deleteCard(@Argument String mobileNumber) {
        cardService.deleteCard(mobileNumber);
        return mobileNumber;
    }

    @SubscriptionMapping
    public Publisher<CardDTO> onNewCard() {
        return cardService.onNewCard();
    }
}