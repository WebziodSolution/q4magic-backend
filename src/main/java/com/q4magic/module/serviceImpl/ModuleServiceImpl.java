package com.q4magic.module.serviceImpl;

import com.q4magic.common.dto.AssignActionsToModuleDto;
import com.q4magic.common.dto.ModuleDto;
import com.q4magic.common.models.Actions;
import com.q4magic.common.models.ModuleActions;
import com.q4magic.common.models.Modules;
import com.q4magic.common.repository.ActionsRepository;
import com.q4magic.common.repository.FunctionalityRepository;
import com.q4magic.common.repository.ModuleActionsRepository;
import com.q4magic.common.repository.ModulesRepository;
import com.q4magic.module.service.ModuleService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@Service(value = "moduleService")
public class ModuleServiceImpl implements ModuleService {
    private static final Logger errorLogger = LoggerFactory.getLogger("errorLogger");

    @Autowired
    private ModulesRepository moduleRepository;

    @Autowired
    private FunctionalityRepository functionalityRepository;

    @Autowired
    private ActionsRepository actionsRepository;

    @Autowired
    private ModuleActionsRepository moduleActionsRepository;

    @Override
    public ModuleDto createModule(ModuleDto moduleDto) {
        ModuleDto responseDto = new ModuleDto();
        try {
            Modules module = new Modules();
            module.setName(moduleDto.getModuleName());
            module.setFunctionality(this.functionalityRepository.findById(moduleDto.getFunctionalityId()).orElseThrow(() -> new RuntimeException("Functionality not found.")));
            module = this.moduleRepository.save(module);

            AssignActionsToModuleDto assignPolicyToModuleDto = new AssignActionsToModuleDto();
            assignPolicyToModuleDto.setModuleId(module.getId());
            assignPolicyToModuleDto.setActionIds(moduleDto.getActions());
            this.assignPolicies(assignPolicyToModuleDto);

            responseDto.setModuleId(module.getId());
            responseDto.setModuleName(module.getName());
            responseDto.setFunctionalityId(module.getFunctionality().getId());
            responseDto.setFunctionalityName(module.getFunctionality().getName());
            responseDto.setActions(moduleDto.getActions());
        } catch (Exception e) {
            errorLogger.error("createModule service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
        return responseDto;
    }

    @Override
    public Map<String, Object> allModuleListPage(String searchKey, Pageable pageable) {
        Map<String, Object> resbody = new HashMap<>();
        List<ModuleDto> moduleDtos = new ArrayList<>();
        try {
            Page<Modules> moduleList;
            if (searchKey == null || searchKey.equals("")) {
                moduleList = this.moduleRepository.findAll(pageable);
            } else {
                moduleList = this.moduleRepository.getModuleByName(searchKey, pageable);
            }
            resbody.put("getTotalPages", moduleList.getTotalPages());
            resbody.put("getNumber", moduleList.getNumber());
            resbody.put("getSize", moduleList.getSize());
            resbody.put("getTotalRecords", moduleList.getTotalElements());
            for (Modules module : moduleList) {
                ModuleDto moduleDto = new ModuleDto();
                BeanUtils.copyProperties(module, moduleDto);
                moduleDto.setFunctionalityId(module.getFunctionality().getId());
                moduleDto.setFunctionalityName(module.getFunctionality().getName());
                moduleDto.setActions(this.getModulePolicy(module.getId()));
                moduleDtos.add(moduleDto);
            }
        } catch (Exception e) {
            errorLogger.error("allModuleListPage service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
        resbody.put("modulesList", moduleDtos);
        return resbody;
    }

    @Override
    public Map<String, Object> moduleByFunctionalityListPage(Integer functionalityId, String searchKey, Pageable pageable) {
        Map<String, Object> resbody = new HashMap<>();
        List<ModuleDto> moduleDtos = new ArrayList<>();
        try {
            Page<Modules> moduleList;
            if (searchKey == null || searchKey.equals("")) {
                moduleList = this.moduleRepository.findModulesByFunctionalityId(functionalityId, pageable);
            } else {
                moduleList = this.moduleRepository.findModulesByFunctionalityIdAndName(functionalityId, searchKey, pageable);
            }
            resbody.put("getTotalPages", moduleList.getTotalPages());
            resbody.put("getNumber", moduleList.getNumber());
            resbody.put("getSize", moduleList.getSize());
            resbody.put("getTotalRecords", moduleList.getTotalElements());
            for (Modules module : moduleList) {
                ModuleDto moduleDto = new ModuleDto();
                BeanUtils.copyProperties(module, moduleDto);
                moduleDto.setFunctionalityId(module.getFunctionality().getId());
                moduleDto.setFunctionalityName(module.getFunctionality().getName());
                moduleDto.setActions(this.getModulePolicy(module.getId()));
                moduleDtos.add(moduleDto);
            }
        } catch (Exception e) {
            errorLogger.error("moduleByFunctionalityListPage service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
        resbody.put("modulesList", moduleDtos);
        return resbody;
    }

    @Override
    public Map<String, Object> getAllModules() {
        Map<String, Object> resbody = new HashMap<>();
        List<ModuleDto> moduleDtos = new ArrayList<>();
        try {
            List<Modules> moduleList = this.moduleRepository.findAll();
            for (Modules module : moduleList) {
                ModuleDto moduleDto = new ModuleDto();
                BeanUtils.copyProperties(module, moduleDto);
                moduleDto.setFunctionalityId(module.getFunctionality().getId());
                moduleDto.setFunctionalityName(module.getFunctionality().getName());
                moduleDto.setActions(this.getModulePolicy(module.getId()));
                moduleDtos.add(moduleDto);
            }
        } catch (Exception e) {
            errorLogger.error("getAllModules service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
        resbody.put("modulesList", moduleDtos);
        return resbody;
    }

    @Override
    public ModuleDto getModuleById(Integer moduleId) {
        ModuleDto responseDto = new ModuleDto();
        try {
            Modules module = this.moduleRepository.findModuleById(moduleId);
            responseDto.setModuleId(module.getId());
            responseDto.setModuleName(module.getName());
            BeanUtils.copyProperties(module, responseDto);
            responseDto.setFunctionalityId(module.getFunctionality().getId());
            responseDto.setFunctionalityName(module.getFunctionality().getName());
            responseDto.setActions(this.getModulePolicy(module.getId()));
        } catch (Exception e) {
            errorLogger.error("getModuleById service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
        return responseDto;
    }

    @Override
    public ModuleDto updateModuleById(Integer moduleId, ModuleDto moduleDto) throws Exception {
        ModuleDto responseDto = new ModuleDto();
        Modules module = this.moduleRepository.findModuleById(moduleId);
        if (module != null) {
            module.setName(moduleDto.getModuleName());
            module.setFunctionality(this.functionalityRepository.getById(moduleDto.getFunctionalityId()));
            module = this.moduleRepository.save(module);

            AssignActionsToModuleDto assignPolicyToModuleDto = new AssignActionsToModuleDto();
            assignPolicyToModuleDto.setModuleId(module.getId());
            assignPolicyToModuleDto.setActionIds(moduleDto.getActions());
            this.assignPolicies(assignPolicyToModuleDto);

            BeanUtils.copyProperties(module, responseDto);
            responseDto.setFunctionalityId(module.getFunctionality().getId());
            responseDto.setFunctionalityName(module.getFunctionality().getName());
            responseDto.setActions(moduleDto.getActions());
        } else {
            throw new Exception("Such Module Doesn't Exist");
        }
        return responseDto;
    }

    @Transactional
    @Override
    public void assignPolicies(AssignActionsToModuleDto assignActionsToModuleDto) throws Exception {
        try {
            Modules module = this.moduleRepository.findModuleById(assignActionsToModuleDto.getModuleId());
            try {
                this.moduleActionsRepository.deleteModuleActionByIds(
                        module.getModuleActions().stream().map(ModuleActions::getId).collect(Collectors.toList())
                );
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }

            for (Integer actionId : assignActionsToModuleDto.getActionIds()) {
                Actions actions = this.actionsRepository.findActionById(actionId);
                ModuleActions storeModuleActions = new ModuleActions();
                storeModuleActions.setModule(module);
                storeModuleActions.setAction(actions);
                this.moduleActionsRepository.save(storeModuleActions);
            }
        } catch (Exception e) {
            errorLogger.error("assignPolicies service Error: " + e);
            throw new RuntimeException(e.getMessage());
        }
    }


    @Override
    public void deleteModuleById(Integer moduleId) throws Exception {
        try {
            this.moduleRepository.deleteById(moduleId);
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public List<Integer> getModulePolicy(Integer moduleId) {
        List<Integer> lst = new ArrayList<>();
        Modules module = this.moduleRepository.findModuleById(moduleId);
        try {
            Set<ModuleActions> modulePolicySet = module.getModuleActions();
            lst = modulePolicySet.stream().map(e -> e.getAction().getId()).sorted().collect(toList());
        } catch (Exception ignored) {
            throw new RuntimeException(ignored.getMessage());
        }
        return lst;
    }
}
