package com.q4magic.common.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class SubscriptionRatesDto {
    private Integer id;
    private String licenseType;
    private Float amount;
    private Date beginDate;
    private Date endDate;
    private String subscriptionRatesCol;
}
