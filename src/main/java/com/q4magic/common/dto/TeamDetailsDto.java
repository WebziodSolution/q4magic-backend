package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class TeamDetailsDto {
    private Integer id;
    private String name;
    private Integer createdBy;
    private String createdByName;
    private String registeredDate;
    private Integer assignMember;
    private String assignMemberName;
    private List<TeamMembersDto> teamMembers;
    private String assignedOpportunities;
}
