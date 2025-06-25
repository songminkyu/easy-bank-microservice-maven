package io.github.songminkyu.account.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.logging")
@Getter
@Setter
public class LoggingProperties {

    private Loki loki = new Loki();

    @Getter
    @Setter
    public static class Loki {
        private  boolean enabled = LoggingDefaults.Loki.ENABLED;
        private  String url = LoggingDefaults.Loki.URL;
    }
}
