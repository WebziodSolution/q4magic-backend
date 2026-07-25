package com.q4magic.common.dto;

import lombok.Data;

@Data
public class ForgotPasswordDto {
    private String username;
    private String question;
    private String answer;
    private Integer questionId;
}
