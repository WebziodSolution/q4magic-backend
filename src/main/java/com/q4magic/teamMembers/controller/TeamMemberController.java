package com.q4magic.teamMembers.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.TeamMembersDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.teamMembers.service.TeamMembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/teamMembers")
public class TeamMemberController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private TeamMembersService teamMembersService;

    @GetMapping("/get/all/{id}")
    public ApiResponse<?> getAllTeamMembers(
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Team members fetched successfully", this.teamMembersService.getAllTeamMembersByTeamId(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch team members", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getTeamMembers(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Team members fetched successfully", this.teamMembersService.getTeamMemberById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch team members", resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createTeamMembers(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody TeamMembersDto teamMembersDto
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Team members added successfully", this.teamMembersService.addTeamMember(teamMembersDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to add team members", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateTeamMembers(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody TeamMembersDto teamMembersDto,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Team members updated successfully", this.teamMembersService.updateTeamMember(id, teamMembersDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update team members", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteTeamMembers(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.teamMembersService.removeTeamMember(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Team members deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete team members", resBody);
        }
    }

    @PatchMapping("/assignOpp/{id}")
    public ApiResponse<?> assignOpportunitiesToTeamMember(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @PathVariable("id") Integer id,
            @RequestBody List<Integer> opportunities
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.teamMembersService.assignOpportunitiesToTeamMember(id, opportunities);
            return new ApiResponse<>(HttpStatus.OK.value(), "Opportunities assigned successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to assign opportunities", resBody);
        }
    }

}
