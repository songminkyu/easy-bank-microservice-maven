package io.github.songminkyu.eurekaserver.config;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityDefaults {

    public static final String CONTENT_SECURITY_POLICY = "script-src 'self'";
}
