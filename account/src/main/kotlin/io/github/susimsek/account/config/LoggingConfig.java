package io.github.susimsek.account.config;

import static jakarta.servlet.DispatcherType.ASYNC;
import static jakarta.servlet.DispatcherType.REQUEST;
import static org.slf4j.Logger.ROOT_LOGGER_NAME;

import ch.qos.logback.classic.LoggerContext;
import com.github.loki4j.logback.AbstractLoki4jEncoder;
import com.github.loki4j.logback.JavaHttpSender;
import com.github.loki4j.logback.JsonEncoder;
import com.github.loki4j.logback.JsonLayout;
import com.github.loki4j.logback.Loki4jAppender;
import feign.Logger;
import io.github.susimsek.account.aspect.LoggingAspect;
import io.github.susimsek.account.constants.Constants;
import io.github.susimsek.account.logging.core.Sink;
import io.github.susimsek.account.logging.feign.DefaultFeignLogger;
import io.github.susimsek.account.logging.restclient.LoggingInterceptor;
import io.github.susimsek.account.logging.servlet.LoggingFilter;
import io.github.susimsek.account.logging.webclient.LoggingExchangeFilterFunction;
import jakarta.servlet.Filter;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.web.reactive.function.client.ExchangeFunction;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(LoggingProperties.class)
@EnableAspectJAutoProxy
public class LoggingConfig {

    private static final String LOKI_APPENDER_NAME = "LOKI";
    private final String appName;

    private static final String CUSTOMIZER_NAME = "loggingClientExchangeFunction";
    private static final String INTERCEPTOR_NAME = "loggingInterceptor";
    private static final String FILTER_NAME = "logging.filter";

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
    @ConditionalOnMissingBean(name = FILTER_NAME)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(jakarta.servlet.Servlet.class)
    public FilterRegistrationBean<?> loggingFilter(Sink sink) {
        var filter = new LoggingFilter(sink);
        return newFilter(filter, FILTER_NAME, Ordered.LOWEST_PRECEDENCE);
    }

    @Bean
    @ConditionalOnMissingBean(Logger.class)
    @ConditionalOnClass(Logger.class)
    public Logger defaultFeignLogger(Sink sink) {
        return new DefaultFeignLogger(sink);
    }

    @Bean
    @ConditionalOnMissingBean(name = CUSTOMIZER_NAME)
    @ConditionalOnClass(ExchangeFunction.class)
    public LoggingExchangeFilterFunction loggingClientExchangeFunction(Sink sink) {
        return new LoggingExchangeFilterFunction(sink);
    }

    @Bean
    @ConditionalOnMissingBean(name = INTERCEPTOR_NAME)
    @ConditionalOnClass(ClientHttpRequestExecution.class)
    public LoggingInterceptor loggingInterceptor(Sink sink) {
        return new LoggingInterceptor(sink);
    }

    static FilterRegistrationBean<?> newFilter(final Filter filter, final String filterName, final int order) {
        final FilterRegistrationBean<?> registration = new FilterRegistrationBean<>(filter);
        registration.setName(filterName);
        registration.setDispatcherTypes(REQUEST, ASYNC);
        registration.setOrder(order);
        return registration;
    }
}
