package io.github.songminkyu.card.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CardConstants {
    public static final String CREDIT_CARD = "Credit Card";
    public static final int NEW_CARD_LIMIT = 1_00_000;
    public static final String STATUS_201 = "201";
    public static final String MESSAGE_201 = "Card created successfully";
    public static final String STATUS_200 = "200";
    public static final String MESSAGE_200 = "Request processed successfully";
    public static final String STATUS_417 = "417";
    public static final String MESSAGE_417_UPDATE = "Update operation failed. Please try again or contact Dev team";
    public static final String MESSAGE_417_DELETE = "Delete operation failed. Please try again or contact Dev team";

    public static final String CARD_RESOURCE_NAME = "Card";
}