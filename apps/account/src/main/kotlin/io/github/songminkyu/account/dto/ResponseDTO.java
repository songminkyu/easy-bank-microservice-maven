package io.github.songminkyu.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
    name = "Response",
    description = "Schema to hold successful response information"
)
public record ResponseDTO(

    @Schema(
        description = "Status code in the response"
    )
    String statusCode,

    @Schema(
        description = "Status message in the response"
    )
    String statusMsg) {

}