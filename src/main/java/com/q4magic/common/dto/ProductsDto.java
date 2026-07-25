package com.q4magic.common.dto;

import lombok.Data;

@Data
public class ProductsDto {
    private Integer id;
    private String salesforceProductId;
    private String type;
    private String name;
    private String code;
    private Float price;
    private String description;
    private Boolean isActive;
    private Integer createdBy;
}
