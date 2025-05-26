package io.github.susimsek.account.util;

import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UUIDGenerator {
    public UUID generateUUIDFromString(String input) {
        return UUID.nameUUIDFromBytes(input.getBytes());
    }
}