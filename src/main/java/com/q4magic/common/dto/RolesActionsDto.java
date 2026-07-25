package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class RolesActionsDto {
    private List<RoleFunctionalityDto> functionalities;
}
