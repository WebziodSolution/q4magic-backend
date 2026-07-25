package com.q4magic.common.dto;

import lombok.Data;

@Data
public class SalesStagesDto {
    private Integer id;
    private String salesforceStageId;
    private Integer crmId;
    private String shortName;
    private String description;
}
