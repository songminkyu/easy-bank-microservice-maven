package io.github.songminkyu.gatewayserver.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constants for Spring Security authorities.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthoritiesConstants {

    public static final String ADMIN = "ROLE_ADMIN";

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String ACCOUNT = "ROLE_ACCOUNT";

    public static final String CARD = "ROLE_CARD";

    public static final String LOAN = "ROLE_LOAN";
}
