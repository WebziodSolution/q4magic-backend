package com.q4magic.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RoleFunctionalityDto implements Serializable {
    private Integer functionalityId;
    private String functionalityName;
    private List<RoleModuleDto> modules;
}
