package io.github.songminkyu.card.config;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import ch.qos.logback.classic.LoggerContext;
import com.github.loki4j.logback.AbstractLoki4jEncoder;
import com.github.loki4j.logback.JavaHttpSender;
import com.github.loki4j.logback.JsonEncoder;
import com.github.loki4j.logback.JsonLayout;
import com.github.loki4j.logback.Loki4jAppender;
import graphql.GraphQL;
import io.github.songminkyu.card.aspect.LoggingAspect;
import io.github.songminkyu.card.constants.Constants;
import io.github.songminkyu.card.logging.core.Sink;
import io.github.songminkyu.card.logging.graphql.LoggingInterceptor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.graphql.server.WebGraphQlInterceptor;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingConfig {

    private static final String LOKI_APPENDER_NAME = "LOKI";
    private final String appName;

    static final String CUSTOMIZER_NAME = "loggingGraphQlInterceptor";

    public LoggingConfig(
        @Value("${spring.application.name}") String appName,
        LoggingProperties loggingProperties
    ) {
        this.appName = appName;
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        if (loggingProperties.getLoki().isEnabled()) {
            addLoki4jAppender(context, loggingProperties.getLoki());
        }
    }

    public void addLoki4jAppender(
        LoggerContext context,
        LoggingProperties.Loki lokiProperties) {
        var loki4jAppender = new Loki4jAppender();
        loki4jAppender.setContext(context);
        loki4jAppender.setName(LOKI_APPENDER_NAME);
        var httpSender = new JavaHttpSender();
        httpSender.setUrl(lokiProperties.getUrl());
        loki4jAppender.setHttp(httpSender);
        var encoder = getJsonEncoder(context);
        loki4jAppender.setFormat(encoder);
        loki4jAppender.start();
        context.getLogger(ROOT_LOGGER_NAME).addAppender(loki4jAppender);
    }

    private JsonEncoder getJsonEncoder(LoggerContext context) {
        var encoder = new JsonEncoder();
        encoder.setContext(context);
        var label = new AbstractLoki4jEncoder.LabelCfg();
        label.setReadMarkers(true);
        label.setPattern("app=" + appName + ",host=${HOSTNAME},level=%level");
        encoder.setLabel(label);
        encoder.setSortByTime(true);
        JsonLayout l = new JsonLayout();
        encoder.setMessage(l);
        encoder.start();
        return encoder;
    }

    @Bean
    @Profile(Constants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }

    @Bean
    @ConditionalOnMissingBean(name = CUSTOMIZER_NAME)
    @ConditionalOnClass(GraphQL.class)
    public WebGraphQlInterceptor loggingGraphQlInterceptor(Sink sink) {
        return new LoggingInterceptor(sink);
    }
}
