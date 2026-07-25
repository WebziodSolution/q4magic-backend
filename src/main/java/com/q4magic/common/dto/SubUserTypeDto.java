package com.q4magic.common.dto;

import lombok.Data;

@Data
public class SubUserTypeDto {
    private Integer id;
    private String name;
    private Integer createdBy;
    private RolesActionsDto rolesActions;
}
