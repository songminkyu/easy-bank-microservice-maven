package io.github.songminkyu.eurekaserver.config;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LoggingDefaults {
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Loki {
        public static final  boolean ENABLED = false;
        public static final String URL = "http://localhost:3100/loki/api/v1/push";
    }
}
