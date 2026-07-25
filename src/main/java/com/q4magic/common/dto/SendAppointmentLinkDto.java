package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class SendAppointmentLinkDto {
    private List<String> emailsList;
    private String message;
}
