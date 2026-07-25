package com.q4magic.common.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class EmailMessageSignatureResultDto {
    private String messageId;
    private String subject;
    private String from;
    private OffsetDateTime receivedAt;
    private SignatureDetailsDto signature;
    private String error; // for failed messages
}
