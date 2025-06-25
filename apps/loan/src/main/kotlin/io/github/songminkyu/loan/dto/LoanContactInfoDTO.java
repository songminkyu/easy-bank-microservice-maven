package io.github.songminkyu.loan.dto;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "loan")
@Getter
@Setter
public class LoanContactInfoDTO {

    private String message;
    private Map<String, String> contactDetails;
    private List<String> onCallSupport;

}