package com.q4magic.processName.service;

import com.q4magic.common.dto.ProcessNameDto;

import java.util.List;

public interface ProcessNameService {

    List<ProcessNameDto> getAllByOpportunity(Integer oppId);

    List<ProcessNameDto> getAllByCustomer(Integer createdBy);

    ProcessNameDto getProcessName(Integer id);

    ProcessNameDto createProcessName(ProcessNameDto processNameDto);

    ProcessNameDto updateProcessName(Integer id, ProcessNameDto processNameDto);

    void deleteProcessName(Integer id);
}
