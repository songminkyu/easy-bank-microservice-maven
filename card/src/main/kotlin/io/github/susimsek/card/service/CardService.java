package io.github.susimsek.card.service;

import io.github.susimsek.card.dto.CardDTO;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.query.ScrollSubrange;

public interface CardService {

    CardDTO createCard(String mobileNumber);

    CardDTO getCard(String mobileNumber);

    boolean updateCard(String cardNumber, CardDTO card);

    void deleteCard(String mobileNumber);

    Window<CardDTO> getAllCards(ScrollSubrange subrange);

    Flowable<CardDTO> onNewCard();
}
