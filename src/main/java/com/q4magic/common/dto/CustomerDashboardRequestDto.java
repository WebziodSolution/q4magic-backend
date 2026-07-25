package com.q4magic.common.dto;

import lombok.Data;

@Data
public class CustomerDashboardRequestDto {
    private Integer customerId;
    private String startDate;
    private String endDate;
}
