package io.github.susimsek.loan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import org.springframework.http.HttpStatus;

@Schema(
    name = "ErrorResponse",
    description = "Schema to hold error response information"
)
public record ErrorResponseDTO(

    @Schema(
        description = "API path invoked by client"
    )
    String apiPath,

    @Schema(
        description = "Error code representing the error happened"
    )
    HttpStatus errorCode,

    @Schema(
        description = "Error message representing the error happened"
    )
    String errorMessage,

    @Schema(
        description = "Time representing when the error happened"
    )
    Instant errorTime) {

}
