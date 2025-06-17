package io.github.songminkyu.gatewayserver.excetion;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorConstants {
    public static final String ERR_VALIDATION = "Validation error";
    public static final String ERR_INTERNAL_SERVER = "Unexpected condition was encountered";
    public static final String PROBLEM_VIOLATION_KEY = "violations";
}