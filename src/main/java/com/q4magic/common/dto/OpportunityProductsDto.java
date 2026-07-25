package com.q4magic.common.dto;

import lombok.Data;

import java.util.Date;

@Data
public class OpportunityProductsDto {
    private Integer id;
    private Integer oppId;
    private Integer productId;
    private String opportunityProductId;
    private String name;
    private Float price;
    private Integer qty;
    private String createdDate;
    private Boolean isDeleted;
}
