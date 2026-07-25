package com.q4magic.common.dto;

import lombok.Data;

@Data
public class SyncStatusDto {
    private Integer id;
    private String statusMessage;
    private Integer customerId;
    private Integer status;
}