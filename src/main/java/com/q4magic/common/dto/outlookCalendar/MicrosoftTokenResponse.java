package com.q4magic.common.dto.outlookCalendar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MicrosoftTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    private String scope;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;
}