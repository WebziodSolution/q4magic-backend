package com.q4magic.functionality.service;


import com.q4magic.common.dto.FunctionalityDto;
import com.q4magic.common.models.Functionality;

import java.util.List;

public interface FunctionalityService {

    List<Functionality> getAllFunctionality();

    Functionality getFunctionality(Integer functionalityId);

    FunctionalityDto createFunctionality(FunctionalityDto functionalityDto);

    FunctionalityDto updateFunctionality(Integer functionalityId, FunctionalityDto functionalityDto);

    void deleteFunctionality(Integer functionalityId);
}
