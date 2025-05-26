package io.github.susimsek.card.debezium.data;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Operation {
    CREATE("c"),
    UPDATE("u"),
    DELETE("r");

    @Getter(onMethod_ = @JsonValue)
    private final String code;
}