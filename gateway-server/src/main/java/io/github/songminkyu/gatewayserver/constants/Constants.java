package io.github.songminkyu.gatewayserver.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

    public static final String SPRING_PROFILE_DEVELOPMENT = "local";
    public static final String SPRING_PROFILE_PRODUCTION = "prod";
}
