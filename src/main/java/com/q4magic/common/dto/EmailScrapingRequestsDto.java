package com.q4magic.common.dto;

import lombok.Data;

@Data
public class EmailScrapingRequestsDto {
    private Integer id;
    private String email;
    private String password;
    private String protocol;
    private String imapHost;
    private Integer imapPort;
    private Integer maxMessages;
    private Integer status;
    private Integer createdBy;
}
