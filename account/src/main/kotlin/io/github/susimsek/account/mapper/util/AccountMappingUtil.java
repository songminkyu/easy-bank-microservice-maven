package io.github.susimsek.account.mapper.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.mapstruct.Named;
import org.mapstruct.Qualifier;

@UtilityClass
public class AccountMappingUtil {

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface AccountNumber {
    }

    @Qualifier
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.SOURCE)
    public @interface AccountType {
    }

    @AccountNumber
    public Long accountNumber(Map<String, Object> in) {
        return (Long) in.get("accountNumber");
    }

    @AccountType
    public String accountType(Map<String, Object> in) {
        return (String) in.get("accountType");
    }

    @Named("branchAddress")
    public String branchAddress(Map<String, Object> in) {
        return (String) in.get("branchAddress");
    }

}