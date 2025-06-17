package io.github.songminkyu.account.debezium.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Map;

public record Payload<T extends Object>(
    T before,
    T after,
    Map<String, Object> source,
    @JsonProperty("op") Operation operation,
    @JsonProperty("ts_ms") Instant date) {
}