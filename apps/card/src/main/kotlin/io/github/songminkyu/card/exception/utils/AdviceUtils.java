package io.github.songminkyu.card.exception.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import lombok.experimental.UtilityClass;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

@UtilityClass
public final class AdviceUtils {

    public void setHttpResponse(
        final ResponseEntity<ProblemDetail> entity,
        final HttpServletResponse response,
        final ObjectMapper mapper) throws IOException {
        response.setStatus(entity.getStatusCode().value());
        PrintWriter out = response.getWriter();
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        out.print(mapper.writeValueAsString(entity.getBody()));
        out.flush();
    }
}