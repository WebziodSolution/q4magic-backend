package com.q4magic.subUserType.serviceImpl;

import com.q4magic.common.dto.RoleFunctionalityDto;
import com.q4magic.common.dto.RoleModuleDto;
import com.q4magic.common.dto.RolesActionsDto;
import com.q4magic.common.dto.SubUserTypeDto;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.subUserType.service.SubUserTypeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service(value = "SubUserTypeService")
public class SubUserTypeServiceImpl implements SubUserTypeService {

    @Autowired
    private SubUserTypeRepository subUserTypeRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private RoleModuleActionsRepository roleModuleActionsRepository;

    @Autowired
    private ActionsRepository actionsRepository;

    @Autowired
    private ModuleActionsRepository moduleActionsRepository;

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private ModulesRepository moduleRepository;

    @Override
    public Map<String, Object> rolesList(String searchKey, Pageable pageable) {
        Map<String, Object> resbody = new HashMap<>();
        List<SubUserTypeDto> rolesDtos = new ArrayList<>();
        try {
            Page<SubUserType> rolesList;
            if (searchKey == null || searchKey.equals("")) {
                rolesList = this.subUserTypeRepository.findAll(pageable);
            } else {
                rolesList = this.subUserTypeRepository.getRolesByName(searchKey, pageable);
            }
            resbody.put("getTotalPages", rolesList.getTotalPages());
            resbody.put("getNumber", rolesList.getNumber());
            resbody.put("getSize", rolesList.getSize());
            resbody.put("getTotalRecords", rolesList.getTotalElements());
            for (SubUserType role : rolesList) {
                SubUserTypeDto rolesDto = new SubUserTypeDto();
                BeanUtils.copyProperties(role, rolesDto);
                rolesDto.setRolesActions(this.getPolicy(role.getId()));
                rolesDtos.add(rolesDto);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        resbody.put("rolesList", rolesDtos);
        return resbody;
    }
    @Override
    public SubUserTypeDto getSubUserTypeById(Integer id) {
        try {
            SubUserType subUserType = this.subUserTypeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SubUserType not found with id: " + id));
            SubUserTypeDto subUserTypeDto = new SubUserTypeDto();
            subUserTypeDto.setId(subUserType.getId());
            subUserTypeDto.setName(subUserType.getName());
            subUserTypeDto.setCreatedBy(subUserType.getCustomers().getId());
            subUserTypeDto.setRolesActions(this.getPolicy(subUserType.getId()));
            return subUserTypeDto;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<SubUserTypeDto> getAllSubUserTypes(Integer id) {
        try {
            List<SubUserType> subUserTypes = this.subUserTypeRepository.findCreatedBy(id);
            List<SubUserTypeDto> subUserTypeDtoList = new ArrayList<>();
            if (!subUserTypes.isEmpty()) {
                for (SubUserType subUserType : subUserTypes) {
                    subUserTypeDtoList.add(this.getSubUserTypeById(subUserType.getId()));
                }
            }
            return subUserTypeDtoList;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SubUserTypeDto createSubUserType(SubUserTypeDto subUserTypeDto) {
        try {
            SubUserType isExist = this.subUserTypeRepository.isExits(subUserTypeDto.getCreatedBy(), subUserTypeDto.getName());
            if (isExist != null) {
                throw new RuntimeException("Role already exists with name: " + subUserTypeDto.getName());
            }
            SubUserType subUserType = new SubUserType();
            subUserType.setName(subUserTypeDto.getName());
            Customers customer = customersRepository.findById(subUserTypeDto.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + subUserTypeDto.getCreatedBy()));
            subUserType.setCustomers(customer);
            this.subUserTypeRepository.save(subUserType);
            this.savePolicy(subUserType.getId(), subUserTypeDto.getRolesActions());
            subUserTypeDto.setRolesActions(subUserTypeDto.getRolesActions());
            return subUserTypeDto;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SubUserTypeDto updateSubUserType(Integer id, SubUserTypeDto subUserTypeDto) {
        try {
            SubUserType isExist = this.subUserTypeRepository.isNotEqual(subUserTypeDto.getCreatedBy(), subUserTypeDto.getName(), id);
            if (isExist != null) {
                throw new RuntimeException("Role already exists with name: " + subUserTypeDto.getName());
            }
            SubUserType subUserType = this.subUserTypeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SubUserType not found with id: " + id));
            subUserType.setName(subUserTypeDto.getName());
            this.subUserTypeRepository.save(subUserType);
            this.savePolicy(subUserType.getId(), subUserTypeDto.getRolesActions());
            return subUserTypeDto;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSubUserType(Integer id) {
        try {
            SubUserType subUserType = this.subUserTypeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("SubUserType not found with id: " + id));
            this.subUserTypeRepository.delete(subUserType);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RolesActionsDto getPolicy(Integer roleId) throws Exception {
        RolesActionsDto rolePolicyDto = new RolesActionsDto();
        List<RoleFunctionalityDto> functionalities = new ArrayList<>();
        if (roleId != 0L) {
            SubUserType role = null;
            try {
                role = this.subUserTypeRepository.findById(roleId).orElseThrow(() -> new Exception("No such Role Exist"));
            } catch (Exception e) {
                throw new Exception("No such Role Exist");
            }
        }
        List<Functionality> functionalityList = this.functionalityRepository.findAll();
        functionalityList.forEach(functionality -> {
            List<RoleModuleDto> modules = new ArrayList<>();
            List<Modules> moduleList = this.moduleRepository.findModulesByFunctionalityId(functionality.getId());
            moduleList.forEach(module -> {
                Set<ModuleActions> modulePolicySet = module.getModuleActions();
                List<Integer> moduleAssignedPolicy = new ArrayList<>();
                List<Integer> roleAssignedPolicy = new ArrayList<>();
                try {
                    moduleAssignedPolicy = modulePolicySet.stream().map(e -> e.getAction().getId()).sorted().collect(Collectors.toList());
                    if (roleId != 0L) {
                        roleAssignedPolicy = this.roleModuleActionsRepository.findModulesByRoleIdAndMpIds(roleId, modulePolicySet.stream().map(ModuleActions::getId).collect(Collectors.toList()))
                                .stream().map(Actions::getId).sorted().collect(Collectors.toList());
                    } else {
                        roleAssignedPolicy = new ArrayList<>();
                    }
                } catch (Exception ignored) {
                }
                try {
                    roleAssignedPolicy = roleAssignedPolicy.isEmpty() ? new ArrayList<>() : roleAssignedPolicy;
                } catch (Exception e) {
                    roleAssignedPolicy = new ArrayList<>();
                }
                RoleModuleDto roleModuleDto = new RoleModuleDto();
                roleModuleDto.setModuleId(module.getId());
                roleModuleDto.setModuleName(module.getName());
                roleModuleDto.setModuleAssignedActions(moduleAssignedPolicy);
                roleModuleDto.setRoleAssignedActions(roleAssignedPolicy);
                modules.add(roleModuleDto);
            });
            RoleFunctionalityDto roleFunctionalityDto = new RoleFunctionalityDto();
            roleFunctionalityDto.setFunctionalityId(functionality.getId());
            roleFunctionalityDto.setFunctionalityName(functionality.getName());
            roleFunctionalityDto.setModules(modules);
            functionalities.add(roleFunctionalityDto);
        });
        rolePolicyDto.setFunctionalities(functionalities);
        return rolePolicyDto;
    }

    @Transactional
    public RolesActionsDto savePolicy(Integer roleId, RolesActionsDto roleActionDto) throws Exception {
        SubUserType role = this.subUserTypeRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        List<RoleModuleActions> roleModulePolicyList = this.roleModuleActionsRepository.findByRoleId(roleId);
        try {
            this.roleModuleActionsRepository.deleteByRoleId(roleId);
        } catch (Exception ignored) {
        }

        roleActionDto.getFunctionalities().forEach(functionality -> {
            functionality.getModules().forEach(module -> {
                if (!module.getRoleAssignedActions().isEmpty()) {
                    module.getRoleAssignedActions().forEach(e -> {
                        Actions policy = this.actionsRepository.findActionById(e);
                        ModuleActions modulePolicy = this.moduleActionsRepository.findModuleActionsByModuleAndActions(module.getModuleId(), policy.getId());
                        RoleModuleActions roleModulePolicy = new RoleModuleActions();
                        roleModulePolicy.setRole(role);
                        roleModulePolicy.setModuleActions(modulePolicy);
                        this.roleModuleActionsRepository.save(roleModulePolicy);
                    });
                }
            });
        });
        return roleActionDto;
    }
}
