package com.q4magic.common.dto;

import lombok.Data;

@Data
public class TeamMembersDto {
    private Integer id;
    private Integer teamId;
    private Integer memberId;
    private String memberName;
    private String role;
    private String title;
    private String opportunities;
    private String email;
}
