package com.q4magic.common.dto;

import lombok.Data;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class EmailFetchRequestDto {
    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String password;

    private String protocol; // imaps or imap
    private String imapHost;
    private Integer imapPort;
    private String folder;
    private int maxMessages = 5; // default last 5 emails
}