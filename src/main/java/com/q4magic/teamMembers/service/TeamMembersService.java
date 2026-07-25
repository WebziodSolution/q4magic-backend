package com.q4magic.teamMembers.service;

import com.q4magic.common.dto.TeamDetailsDto;
import com.q4magic.common.dto.TeamMembersDto;

import java.util.List;

public interface TeamMembersService {
    List<TeamMembersDto> getAllTeamMembersByTeamId(Integer teamId);

    TeamMembersDto getTeamMemberById(Integer teamMemberId);

    TeamMembersDto addTeamMember(TeamMembersDto teamMembersDto);

    TeamMembersDto updateTeamMember(Integer teamMemberId, TeamMembersDto teamMembersDto);

    void removeTeamMember(Integer teamMemberId);

    void assignOpportunitiesToTeamMember(Integer teamMemberId, List<Integer> opportunities);
}
