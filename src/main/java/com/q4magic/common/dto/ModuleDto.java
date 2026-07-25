package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModuleDto {
    private Integer moduleId;
    private String moduleName;
    private Integer functionalityId;
    private String functionalityName;
    private List<Integer> actions;
}
