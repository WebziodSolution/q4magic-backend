package com.q4magic.crm.service;


import com.q4magic.common.dto.CRMDto;

import java.util.List;

public interface CrmService {

    List<CRMDto> getAllCRMs();

    CRMDto getCRMById(Integer id);

    CRMDto createCRM(CRMDto crmDto);

    CRMDto updateCRM(Integer id, CRMDto crmDto);

    void deleteCRM(Integer id);
}
