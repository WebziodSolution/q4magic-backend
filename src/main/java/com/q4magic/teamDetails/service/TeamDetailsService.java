package com.q4magic.teamDetails.service;

import com.q4magic.common.dto.TeamDetailsDto;

import java.util.List;
import java.util.Map;

public interface TeamDetailsService {

    Map<String, Object> getAllTeamAndMembers(Integer createdBy);

    List<TeamDetailsDto> getAllTeamDetails(Integer createdBy);

    TeamDetailsDto getTeamDetailsById(Integer id);

    TeamDetailsDto createTeamDetails(TeamDetailsDto teamDetailsDto);

    TeamDetailsDto updateTeamDetails(Integer id, TeamDetailsDto teamDetailsDto);

    void deleteTeamDetails(Integer id);

}
