package com.q4magic.teamMembers.serviceImpl;

import com.q4magic.common.dto.TeamDetailsDto;
import com.q4magic.common.dto.TeamMembersDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.TeamDetails;
import com.q4magic.common.models.TeamMembers;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.TeamDetailsRepository;
import com.q4magic.common.repository.TeamMembersRepository;
import com.q4magic.teamMembers.service.TeamMembersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service(value = "TeamMembersService")
public class TeamMembersServiceImpl implements TeamMembersService {

    @Autowired
    private TeamMembersRepository teamMembersRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private TeamDetailsRepository teamDetailsRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public List<TeamMembersDto> getAllTeamMembersByTeamId(Integer teamId) {
        try {
            List<TeamMembers> teamMembersList = this.teamMembersRepository.findByTeamId(teamId);
            List<TeamMembersDto> teamMembersDtoList = new ArrayList<>();
            if (!teamMembersList.isEmpty()) {
                for (TeamMembers teamMembers : teamMembersList) {
                    teamMembersDtoList.add(this.getTeamMemberById(teamMembers.getId()));
                }
            }
            return teamMembersDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TeamMembersDto getTeamMemberById(Integer teamMemberId) {
        try {
            TeamMembers teamMembers = this.teamMembersRepository.findById(teamMemberId).orElseThrow(() -> new RuntimeException("Team member not found with id: " + teamMemberId));
            TeamMembersDto teamMembersDto = new TeamMembersDto();
            teamMembersDto.setId(teamMembers.getId());
            if (teamMembers.getCustomers() != null) {
                teamMembersDto.setMemberId(teamMembers.getCustomers().getId());
                teamMembersDto.setMemberName((teamMembers.getCustomers().getUsername() != null && !teamMembers.getCustomers().getUsername().isBlank()) ? teamMembers.getCustomers().getUsername() : teamMembers.getCustomers().getFirstName() + " " + teamMembers.getCustomers().getLastName());
                teamMembersDto.setRole(teamMembers.getCustomers().getSubUserType().getName());
                teamMembersDto.setTitle(teamMembers.getCustomers().getTitle());
                teamMembersDto.setEmail(teamMembers.getCustomers().getEmailAddress());
            }
//            if (teamMembers.getCustomers().getName() != null && !teamMembers.getCustomers().getName().isEmpty()) {
//            } else {
//                teamMembersDto.setMemberName(teamMembers.getCustomers().getUsername());
//            }
            teamMembersDto.setTeamId(teamMembers.getTeamDetails().getId());
            teamMembersDto.setOpportunities(teamMembers.getOpportunities());
            return teamMembersDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TeamMembersDto addTeamMember(TeamMembersDto teamMembersDto) {
        try {
            TeamMembers teamMembers = new TeamMembers();

            Customers customers = this.customersRepository.findById(teamMembersDto.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + teamMembersDto.getMemberId()));
            TeamDetails teamDetails = this.teamDetailsRepository.findById(teamMembersDto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamMembersDto.getTeamId()));

            teamMembers.setCustomers(customers);
            teamMembers.setTeamDetails(teamDetails);
            teamMembers.setOpportunities(teamMembersDto.getOpportunities());
            TeamMembers savedTeamMember = this.teamMembersRepository.save(teamMembers);
            teamMembersDto.setId(savedTeamMember.getId());
            return teamMembersDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TeamMembersDto updateTeamMember(Integer teamMemberId, TeamMembersDto teamMembersDto) {
        try {
            Customers customers = this.customersRepository.findById(teamMembersDto.getMemberId())
                    .orElseThrow(() -> new RuntimeException("Customer not found with id: " + teamMembersDto.getMemberId()));
            TeamDetails teamDetails = this.teamDetailsRepository.findById(teamMembersDto.getTeamId())
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamMembersDto.getTeamId()));

            TeamMembers teamMembers = this.teamMembersRepository.findById(teamMemberId)
                    .orElseThrow(() -> new RuntimeException("Team member not found with id: " + teamMemberId));

            teamMembers.setCustomers(customers);
            teamMembers.setTeamDetails(teamDetails);

            // Parse existing opportunities safely
            List<Integer> opportunityIdList = new ArrayList<>();
            if (teamMembers.getOpportunities() != null && !teamMembers.getOpportunities().isEmpty()) {
                String cleaned = teamMembers.getOpportunities().replace("[", "").replace("]", "");
                if (!cleaned.isBlank()) {
                    opportunityIdList = Arrays.stream(cleaned.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());
                }
            }

            // Parse new opportunities from DTO safely
            if (teamMembersDto.getOpportunities() != null && !teamMembersDto.getOpportunities().isEmpty()) {
                String cleanedDto = teamMembersDto.getOpportunities().replace("[", "").replace("]", "");
                if (!cleanedDto.isBlank()) {
                    List<Integer> newOpportunityIds = Arrays.stream(cleanedDto.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .map(Integer::parseInt)
                            .collect(Collectors.toList());

                    // Merge without duplicates
                    for (Integer oppId : newOpportunityIds) {
                        if (!opportunityIdList.contains(oppId)) {
                            opportunityIdList.add(oppId);
                        }
                    }
                }
            }

            // Save back as "[2252,2253]" format
            teamMembers.setOpportunities(opportunityIdList.toString());

            TeamMembers savedTeamMember = this.teamMembersRepository.save(teamMembers);
            teamMembersDto.setId(savedTeamMember.getId());
            return teamMembersDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    @Override
    public void removeTeamMember(Integer teamMemberId) {
        try {
            TeamMembers teamMembers = this.teamMembersRepository.findById(teamMemberId).orElseThrow(() -> new RuntimeException("Team member not found with id: " + teamMemberId));
            this.teamMembersRepository.delete(teamMembers);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assignOpportunitiesToTeamMember(Integer teamId, List<Integer> opportunities) {
        try {
            TeamDetails teamDetails = this.teamDetailsRepository.findById(teamId)
                    .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));

            List<TeamMembers> teamMembersList = this.teamMembersRepository.findByTeamId(teamId);

            if (teamMembersList.isEmpty()) {
                throw new RuntimeException("No team members found for teamId: " + teamId);
            }

            // Convert the incoming list to DB string format "[2252,2253]"
            String opportunitiesString = opportunities != null ? opportunities.toString() : "[]";

            for (TeamMembers member : teamMembersList) {
                // Overwrite existing opportunities with exactly what is passed
                member.setOpportunities(opportunitiesString);
                this.teamMembersRepository.save(member);
            }

            // Also set in teamDetails if needed
            teamDetails.setAssignedOpportunities(opportunitiesString);
            this.teamDetailsRepository.save(teamDetails);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


}
