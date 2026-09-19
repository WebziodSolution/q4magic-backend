package com.q4magic.common.dto.outlookCalendar;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MicrosoftExchangeRequest {
    private String code;

    @JsonProperty("redirect_uri")
    private String redirectUri;
}