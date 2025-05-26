package io.github.susimsek.account.constants;

import java.util.Random;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final Random RANDOM = new Random();

    public static final String SPRING_PROFILE_DEVELOPMENT = "local";
    public static final String SPRING_PROFILE_PRODUCTION = "prod";
}
