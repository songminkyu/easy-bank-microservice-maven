package io.github.songminkyu.card.dto;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "card")
@Getter
@Setter
public class CardContactInfoDTO {

    private String message;
    private Map<String, String> contactDetails;
    private List<String> onCallSupport;

}