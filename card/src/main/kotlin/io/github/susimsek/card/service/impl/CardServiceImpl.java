package io.github.susimsek.card.service.impl;

import static io.github.susimsek.card.constants.Constants.RANDOM;

import io.github.susimsek.card.constants.CardConstants;
import io.github.susimsek.card.consumer.CardPublisher;
import io.github.susimsek.card.dto.CardDTO;
import io.github.susimsek.card.entity.Card;
import io.github.susimsek.card.exception.CardAlreadyExistsException;
import io.github.susimsek.card.exception.EntityNotFoundException;
import io.github.susimsek.card.mapper.CardMapper;
import io.github.susimsek.card.repository.CardRepository;
import io.github.susimsek.card.service.CardService;
import io.reactivex.rxjava3.core.Flowable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.graphql.data.query.ScrollSubrange;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    private final CardPublisher cardPublisher;

    private final CardMapper cardMapper;

    @Override
    public CardDTO createCard(String mobileNumber) {
        if (cardRepository.existsByMobileNumber(mobileNumber)) {
            throw new CardAlreadyExistsException("Card already registered with given mobileNumber " + mobileNumber);
        }
        var card = cardRepository.save(createNewCard(mobileNumber));
        return cardMapper.toDto(card);
    }

    @Override
    public CardDTO getCard(String mobileNumber) {
        var card = cardRepository.findByMobileNumber(mobileNumber).orElseThrow(
            () -> new EntityNotFoundException(Card.class, "mobileNumber", mobileNumber)
        );
        return cardMapper.toDto(card);
    }

    @Override
    public boolean updateCard(String cardNumber, CardDTO card) {
        var cardEntity = cardRepository.findByCardNumber(cardNumber).orElseThrow(
            () -> new EntityNotFoundException(Card.class, "cardNumber", cardNumber)
        );
        cardMapper.partialUpdate(cardEntity, card);
        cardEntity.setCardNumber(cardNumber);
        cardRepository.save(cardEntity);
        return true;
    }

    @Override
    public void deleteCard(String mobileNumber) {
        var card = cardRepository.findByMobileNumber(mobileNumber).orElseThrow(
            () -> new EntityNotFoundException(Card.class, "mobileNumber", mobileNumber)
        );
        cardRepository.deleteById(card.getCardId());
    }

    @Override
    public Window<CardDTO> getAllCards(ScrollSubrange subrange) {
        ScrollPosition scrollPosition = subrange.position().orElse(ScrollPosition.offset());
        Limit limit = Limit.of(subrange.count().orElse(10));
        Sort sort = Sort.by("cardId").ascending();
        return cardRepository.findAllBy(scrollPosition, limit, sort)
            .map(cardMapper::toDto);
    }

    @Override
    public Flowable<CardDTO> onNewCard() {
        return cardPublisher.getPublisher();
    }

    private Card createNewCard(String mobileNumber) {
        Card newCard = new Card();
        long randomCardNumber = 100000000000L + RANDOM.nextInt(900000000);
        newCard.setCardNumber(Long.toString(randomCardNumber));
        newCard.setMobileNumber(mobileNumber);
        newCard.setCardType(CardConstants.CREDIT_CARD);
        newCard.setTotalLimit(CardConstants.NEW_CARD_LIMIT);
        newCard.setAmountUsed(0);
        newCard.setAvailableAmount(CardConstants.NEW_CARD_LIMIT);
        return newCard;
    }
}
