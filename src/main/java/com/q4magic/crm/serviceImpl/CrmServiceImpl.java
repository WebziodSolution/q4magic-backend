package com.q4magic.crm.serviceImpl;

import com.q4magic.common.dto.CRMDto;
import com.q4magic.common.models.CRM;
import com.q4magic.common.repository.CRMRepository;
import com.q4magic.crm.service.CrmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "crmService")
public class CrmServiceImpl implements CrmService {
    @Autowired
    private CRMRepository crmRepository;

    @Override
    public List<CRMDto> getAllCRMs() {
        try {
            List<CRM> crmList = this.crmRepository.findAll();
            List<CRMDto> crmDtoList = new ArrayList<>();
            if (!crmList.isEmpty()) {
                for (CRM crm : crmList) {
                    crmDtoList.add(this.getCRMById(crm.getCrmId()));
                }
            }
            return crmDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CRMDto getCRMById(Integer id) {
        try {
            CRM crm = this.crmRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("CRM not found with id: " + id));
            CRMDto crmDto = new CRMDto();
            crmDto.setCrmId(crm.getCrmId());
            crmDto.setName(crm.getName());
            return crmDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CRMDto createCRM(CRMDto crmDto) {
        try {
            CRM crm = new CRM();
            crm.setName(crmDto.getName());
            this.crmRepository.save(crm);
            return crmDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CRMDto updateCRM(Integer id, CRMDto crmDto) {
        try {
            CRM crm = this.crmRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("CRM not found with id: " + id));
            crm.setName(crmDto.getName());
            this.crmRepository.save(crm);
            return crmDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCRM(Integer id) {
        try {
            CRM crm = this.crmRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("CRM not found with id: " + id));
            this.crmRepository.delete(crm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
