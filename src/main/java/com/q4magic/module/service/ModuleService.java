package com.q4magic.module.service;

import com.q4magic.common.dto.AssignActionsToModuleDto;
import com.q4magic.common.dto.ModuleDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface ModuleService {
    ModuleDto createModule(ModuleDto moduleDto);
    Map<String, Object> allModuleListPage(String searchKey, Pageable pageable);
    Map<String, Object> moduleByFunctionalityListPage(Integer functionalityId, String searchKey, Pageable pageable);
    Map<String, Object> getAllModules();
    ModuleDto getModuleById(Integer moduleId);
    ModuleDto updateModuleById(Integer moduleId, ModuleDto moduleDto) throws Exception;
    void assignPolicies(AssignActionsToModuleDto assignActionsToModuleDto) throws Exception;
    void deleteModuleById(Integer moduleId) throws Exception;
    List<Integer> getModulePolicy(Integer moduleId);
}
