package io.github.songminkyu.card.exception;

public class CardAlreadyExistsException extends RuntimeException {

    public CardAlreadyExistsException(String message) {
        super(message);
    }

}