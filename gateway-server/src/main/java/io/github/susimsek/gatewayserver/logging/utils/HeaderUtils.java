package io.github.susimsek.gatewayserver.logging.utils;

import static java.util.Collections.list;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON_VALUE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

@UtilityClass
public final class HeaderUtils {

    private static final List<String> SUPPORTED_CONTENT_TYPES = List.of(
        APPLICATION_JSON_VALUE,
        APPLICATION_PROBLEM_JSON_VALUE);

    public HttpHeaders toHeaders(Map<String, Collection<String>> feignHeaders) {
        HttpHeaders headers = new HttpHeaders();
        feignHeaders.keySet()
            .forEach(key -> headers.put(key, getHeaderValues(feignHeaders, key)));
        return headers;
    }

    public HttpHeaders toHeaders(final Iterable<Entry<String, String>> entries) {
        HttpHeaders headers = new HttpHeaders();

        for (final Entry<String, String> entry : entries) {
            append(headers, entry);
        }
        return headers;
    }

    public HttpHeaders toHeadersWithEnumHeaders(
        Enumeration<String> headerNames,
        Function<String, Enumeration<String>> getHeaders,
        Function<String, String> getHeader) {
        HttpHeaders headers = new HttpHeaders();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            var previous = headers.get(name);
            if (previous == null) {
                headers.put(name, list(getHeaders.apply(name)));
            } else {
                previous.add(getHeader.apply(name));
                headers.put(name, previous);
            }
        }
        return headers;
    }

    public HttpHeaders toHeaders(
        Collection<String> headerNames,
        Function<String, Collection<String>> getHeaders,
        Function<String, String> getHeader) {
        HttpHeaders headers = new HttpHeaders();
        for (final String name : headerNames) {
            var previous = headers.get(name);
            if (previous == null) {
                headers.put(name, new ArrayList<>(getHeaders.apply(name)));
            } else {
                previous.add(getHeader.apply(name));
                headers.put(name, previous);
            }
        }
        return headers;
    }

    private List<String> getHeaderValues(Map<String, Collection<String>> feignHeaders, String headerName) {
        Collection<String> values = feignHeaders.get(headerName);
        if (!CollectionUtils.isEmpty(values)) {
            return new ArrayList<>(values);
        }
        return new ArrayList<>();
    }

    private void append(
        final HttpHeaders headers, final Entry<String, String> entry) {
        var previous = headers.get(entry.getKey());
        if (previous == null) {
            headers.put(entry.getKey(), singletonList(entry.getValue()));
        } else {
            previous.add(entry.getValue());
            headers.put(entry.getKey(), previous);
        }
    }

    public boolean isContentTypeSupported(String contentType) {
        return SUPPORTED_CONTENT_TYPES.stream()
            .anyMatch(contentType::startsWith);
    }

}