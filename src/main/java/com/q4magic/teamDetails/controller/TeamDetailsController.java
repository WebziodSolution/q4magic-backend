package com.q4magic.teamDetails.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.TeamDetailsDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.teamDetails.service.TeamDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/teamDetails")
public class TeamDetailsController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private TeamDetailsService teamDetailsService;

    @GetMapping("/getAllTeamAndMembers")
    public ApiResponse<?> getAllTeamAndMembers(
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Team fetched successfully", this.teamDetailsService.getAllTeamAndMembers(loginUserId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch", resBody);
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<?> getAll(
            @RequestHeader(value = "Authorization",required = false) String authorizationHeader,
            @RequestParam(value = "cusId",required = false) Integer cusId
    ) {
        Map<String, Object> resBody = new HashMap<>();
        Integer loginUserId = null;
        if (authorizationHeader != null) {
            loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
        }
        if (cusId != null) {
            loginUserId = cusId;
        }
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Team fetched successfully", this.teamDetailsService.getAllTeamDetails(loginUserId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getTeamDetailsId(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Team fetched successfully", this.teamDetailsService.getTeamDetailsById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch", resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createTeam(
            @RequestHeader(value = "Authorization",required = false) String authorizationHeader,
            @RequestBody TeamDetailsDto teamDetailsDto
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = null;
            if (teamDetailsDto.getCreatedBy() != null) {
                loginUserId = teamDetailsDto.getCreatedBy();
            }else{
                loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            teamDetailsDto.setCreatedBy(loginUserId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Team created successfully", this.teamDetailsService.createTeamDetails(teamDetailsDto));
        } catch (Exception e) {
            e.printStackTrace();
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create team", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateTeam(
            @RequestHeader(value = "Authorization",required = false) String authorizationHeader,
            @RequestBody TeamDetailsDto teamDetailsDto,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer loginUserId = null;
            if (teamDetailsDto.getCreatedBy() != null) {
                loginUserId = teamDetailsDto.getCreatedBy();
            }else{
                loginUserId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            }
            teamDetailsDto.setCreatedBy(loginUserId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Team updated successfully", this.teamDetailsService.updateTeamDetails(id, teamDetailsDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update team", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteTeam(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.teamDetailsService.deleteTeamDetails(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Team deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete team", resBody);
        }
    }
}
