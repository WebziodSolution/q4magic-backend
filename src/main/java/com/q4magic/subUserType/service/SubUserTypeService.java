package com.q4magic.subUserType.service;

import com.q4magic.common.dto.SubUserTypeDto;
import com.q4magic.common.dto.RolesActionsDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SubUserTypeService {
    SubUserTypeDto getSubUserTypeById(Integer id);

    List<SubUserTypeDto> getAllSubUserTypes(Integer id);

    Map<String, Object> rolesList(String searchKey, Pageable pageable);

    SubUserTypeDto createSubUserType(SubUserTypeDto subUserTypeDto);

    SubUserTypeDto updateSubUserType(Integer id, SubUserTypeDto subUserTypeDto);

    void deleteSubUserType(Integer id);

    RolesActionsDto getPolicy(Integer id) throws Exception;

}
