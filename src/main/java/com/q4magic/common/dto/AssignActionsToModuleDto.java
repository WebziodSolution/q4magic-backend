package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignActionsToModuleDto {
    private Integer moduleId;
    private List<Integer> actionIds;
}
