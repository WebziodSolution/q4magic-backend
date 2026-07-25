package com.q4magic.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleModuleDto implements Serializable {
    private Integer moduleId;
    private String moduleName;
    private List<Integer> moduleAssignedActions;
    private List<Integer> roleAssignedActions;
}
