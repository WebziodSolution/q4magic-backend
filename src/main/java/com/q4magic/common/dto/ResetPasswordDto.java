package com.q4magic.common.dto;

import lombok.Data;

@Data
public class ResetPasswordDto {
    String password;
    String userId;
    String token;
}
