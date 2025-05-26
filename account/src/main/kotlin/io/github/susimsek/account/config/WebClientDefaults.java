package io.github.susimsek.account.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebClientDefaults {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Config {
        public static final int CONNECT_TIMEOUT = 5000;
        public static final int READ_TIMEOUT = 10000;
        public static final int WRITE_TIMEOUT = 10000;
    }
}
