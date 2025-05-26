package io.github.susimsek.account.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.webclient")
@Getter
@Setter
public class WebClientProperties {

    private HttpClient httpClient = new HttpClient();
    private Oauth2 oauth2 = new Oauth2();

    @Getter
    @Setter
    public static class Oauth2 {
        private String clientRegistrationId;
    }

    @Getter
    @Setter
    public static class HttpClient {

        private Config config = new Config();

        @Getter
        @Setter
        public static class Config {
            private int connectTimeout = WebClientDefaults.Config.CONNECT_TIMEOUT;
            private int readTimeout = WebClientDefaults.Config.READ_TIMEOUT;
            private int writeTimeout = WebClientDefaults.Config.WRITE_TIMEOUT;
        }
    }
}
