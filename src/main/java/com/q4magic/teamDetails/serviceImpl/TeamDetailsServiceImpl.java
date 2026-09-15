package com.q4magic.teamDetails.serviceImpl;

import com.q4magic.common.dto.TeamDetailsDto;
import com.q4magic.common.dto.TeamMembersDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.TeamDetails;
import com.q4magic.common.models.TeamMembers;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.TeamDetailsRepository;
import com.q4magic.teamDetails.service.TeamDetailsService;
import com.q4magic.teamMembers.service.TeamMembersService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(value = "TeamDetailsService")
public class TeamDetailsServiceImpl implements TeamDetailsService {

    @Autowired
    private TeamDetailsRepository teamDetailsRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private TeamMembersService teamMembersService;

    @Override
    public Map<String, Object> getAllTeamAndMembers(Integer createdBy) {
        try {
            Customers customers = customersRepository.findById(createdBy).orElseThrow(() -> new EntityNotFoundException("Customer not found"));
            Map<String, Object> result = new HashMap<>();

            List<Map<String, Object>> teamsList = new ArrayList<>();
            List<Map<String, Object>> individualsList = new ArrayList<>();

            // ===== 1️⃣ Build Teams and Members =====
            List<TeamDetails> teamDetailsList = this.teamDetailsRepository.findCreatedBy(createdBy);
            if (teamDetailsList == null) {
                if (customers.getCustomers() != null) {
                    teamDetailsList = this.teamDetailsRepository.findCreatedBy(customers.getCustomers().getId());
                }
            }
            if (!teamDetailsList.isEmpty()) {
                for (TeamDetails teamDetails : teamDetailsList) {
                    Map<String, Object> teamMap = new HashMap<>();
                    teamMap.put("name", teamDetails.getName());

                    List<TeamMembersDto> teamMembers = this.teamMembersService.getAllTeamMembersByTeamId(teamDetails.getId());

                    List<Map<String, Object>> memberList = new ArrayList<>();
                    if (!teamMembers.isEmpty()) {
                        for (TeamMembersDto member : teamMembers) {
                            Map<String, Object> memberMap = new HashMap<>();
                            memberMap.put("name", member.getMemberName());
                            memberMap.put("id", member.getMemberId());
                            memberList.add(memberMap);
                        }
                    }

                    teamMap.put("members", memberList);
                    teamsList.add(teamMap);
                }
            }

            // ===== 2️⃣ Build Individuals (Not in any team) =====
            List<Customers> customersList = this.customersRepository.getAllSubUsers(createdBy);
            customersList.add(customers);
            for (Customers customer : customersList) {
                boolean isInTeam = false;

                // Check if this customer is already a team member
                for (Map<String, Object> team : teamsList) {
                    List<Map<String, Object>> members = (List<Map<String, Object>>) team.get("members");
                    for (Map<String, Object> member : members) {
                        if (member.get("id").equals(customer.getId())) {
                            isInTeam = true;
                            break;
                        }
                    }
                    if (isInTeam) break;
                }

                // Add to individuals if not in any team
                if (!isInTeam) {
                    Map<String, Object> individual = new HashMap<>();
                    individual.put("name", (customer.getUsername() != null && !customer.getUsername().isBlank()) ? customer.getUsername() : customer.getFirstName() + " " + customer.getLastName());
                    individual.put("id", customer.getId());
                    individualsList.add(individual);
                }
            }

            // ===== 3️⃣ Final Structure =====
            result.put("teams", teamsList);
            result.put("individuals", individualsList);

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error while fetching teams and members", e);
        }
    }

    @Override
    public List<TeamDetailsDto> getAllTeamDetails(Integer createdBy) {
        try {
            if (createdBy != null) {

                Customers customers = this.customersRepository.findById(createdBy)
                        .orElseThrow(() -> new RuntimeException("Account not found"));
                // Use Set to enforce uniqueness (requires proper equals/hashCode in TeamDetails based on id)
                Set<TeamDetails> teamDetailsSet = new HashSet<>(this.teamDetailsRepository.findCreatedBy(createdBy));

                if (customers.getRole() != null && "SALES REPRESENTIVE".equals(customers.getRole().getRole()) || customers.getRole() != null && "SALES MANAGER".equals(customers.getRole().getRole())) {
                    List<Customers> subUsers = this.customersRepository.getAllSubUsers(createdBy);

                    if (subUsers != null && !subUsers.isEmpty()) {
                        for (Customers subUser : subUsers) {
                            List<TeamDetails> assignedTeams = this.teamDetailsRepository.getAllAssignTeams(subUser.getId());
                            if (assignedTeams != null && !assignedTeams.isEmpty()) {
                                teamDetailsSet.addAll(assignedTeams); // duplicates automatically skipped
                            }
                        }
                    }
                } else {
                    teamDetailsSet.addAll(this.teamDetailsRepository.getAllAssignTeams(createdBy));
                }

                List<TeamDetailsDto> teamDetailsDtoList = new ArrayList<>();
                for (TeamDetails teamDetails : teamDetailsSet) {
                    teamDetailsDtoList.add(this.getTeamDetailsById(teamDetails.getId()));
                }
                return teamDetailsDtoList;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TeamDetailsDto getTeamDetailsById(Integer id) {
        try {
            TeamDetails teamDetails = this.teamDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("TeamDetails not found"));
            TeamDetailsDto teamDetailsDto = new TeamDetailsDto();
            teamDetailsDto.setCreatedBy(teamDetails.getCustomers().getId());
            teamDetailsDto.setCreatedByName(
                    (teamDetails.getCustomers().getUsername() != null && !teamDetails.getCustomers().getUsername().trim().isEmpty())
                            ? teamDetails.getCustomers().getUsername()
                            : teamDetails.getCustomers().getFirstName() + " " + teamDetails.getCustomers().getLastName()
            );
            teamDetailsDto.setTeamMembers(this.teamMembersService.getAllTeamMembersByTeamId(teamDetails.getId()));
            if (teamDetails.getAssignMember() != null) {
                teamDetailsDto.setAssignMember(teamDetails.getAssignMember().getId());
                teamDetailsDto.setAssignMemberName(
                        (teamDetails.getAssignMember().getUsername() != null && !teamDetails.getAssignMember().getUsername().trim().isEmpty())
                                ? teamDetails.getAssignMember().getUsername()
                                : teamDetails.getAssignMember().getFirstName() + " " + teamDetails.getAssignMember().getLastName()
                );

            }
            BeanUtils.copyProperties(teamDetails, teamDetailsDto);
            return teamDetailsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TeamDetailsDto createTeamDetails(TeamDetailsDto teamDetailsDto) {
        try {
            Customers customers = this.customersRepository.findById(teamDetailsDto.getCreatedBy()).orElseThrow(() -> new RuntimeException("Account not found"));
            TeamDetails teamDetails = new TeamDetails();
            teamDetails.setCustomers(customers);
            teamDetails.setRegisteredDate(new Date());
            if (teamDetailsDto.getAssignMember() != null) {
                Customers assignMember = this.customersRepository.findById(teamDetailsDto.getAssignMember()).orElseThrow(() -> new RuntimeException("Account not found"));
                teamDetails.setAssignMember(assignMember);
            }
            BeanUtils.copyProperties(teamDetailsDto, teamDetails, "id");
            TeamDetails savedTeamDetails = this.teamDetailsRepository.save(teamDetails);

            if (teamDetailsDto.getTeamMembers() != null && !teamDetailsDto.getTeamMembers().isEmpty()) {
                for (TeamMembersDto teamMembersDto : teamDetailsDto.getTeamMembers()) {
                    teamMembersDto.setTeamId(savedTeamDetails.getId());
                    this.teamMembersService.addTeamMember(teamMembersDto);
                }
                TeamDetailsDto savedTeamDetailsDto = new TeamDetailsDto();
                BeanUtils.copyProperties(savedTeamDetails, savedTeamDetailsDto);
                return savedTeamDetailsDto;
            }
            teamDetailsDto.setId(savedTeamDetails.getId());
            return teamDetailsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TeamDetailsDto updateTeamDetails(Integer id, TeamDetailsDto teamDetailsDto) {
        try {
            Customers customers = this.customersRepository.findById(teamDetailsDto.getCreatedBy()).orElseThrow(() -> new RuntimeException("Customer not found"));
            TeamDetails teamDetails = this.teamDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("TeamDetails not found"));
            teamDetails.setCustomers(customers);
            if (teamDetailsDto.getAssignMember() != null) {
                Customers assignMember = this.customersRepository.findById(teamDetailsDto.getAssignMember()).orElseThrow(() -> new RuntimeException("Account not found"));
                teamDetails.setAssignMember(assignMember);
            }
            BeanUtils.copyProperties(teamDetailsDto, teamDetails, "id");
            this.teamDetailsRepository.save(teamDetails);

            if (teamDetailsDto.getTeamMembers() != null && !teamDetailsDto.getTeamMembers().isEmpty()) {
                for (TeamMembersDto teamMembersDto : teamDetailsDto.getTeamMembers()) {
                    teamMembersDto.setTeamId(id);
                    if (teamMembersDto.getId() != null) {
                        this.teamMembersService.updateTeamMember(teamMembersDto.getId(), teamMembersDto);
                    } else {
                        this.teamMembersService.addTeamMember(teamMembersDto);
                    }
                }
            }

            return teamDetailsDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTeamDetails(Integer id) {
        try {
            TeamDetails teamDetails = this.teamDetailsRepository.findById(id).orElseThrow(() -> new RuntimeException("TeamDetails not found"));
            this.teamDetailsRepository.delete(teamDetails);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
