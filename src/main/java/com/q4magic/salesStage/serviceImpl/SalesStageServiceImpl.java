package com.q4magic.salesStage.serviceImpl;

import com.q4magic.common.dto.SalesStagesDto;
import com.q4magic.common.models.CRM;
import com.q4magic.common.models.SalesStages;
import com.q4magic.common.repository.CRMRepository;
import com.q4magic.common.repository.SalesStagesRepository;
import com.q4magic.salesStage.service.SalesStageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "SalesStageService")
public class SalesStageServiceImpl implements SalesStageService {

    @Autowired
    private SalesStagesRepository salesStagesRepository;

    @Autowired
    private CRMRepository crmRepository;


    @Override
    public List<SalesStagesDto> getAllSalesStages() {
        try {
            List<SalesStages> salesStagesList = this.salesStagesRepository.findAll();
            List<SalesStagesDto> salesStagesDtoList = new ArrayList<>();
            if (!salesStagesList.isEmpty()){
                for (SalesStages salesStages:salesStagesList){
                    salesStagesDtoList.add(this.getSalesStageById(salesStages.getId()));
                }
            }
            return salesStagesDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SalesStagesDto getSalesStageById(Integer id) {
        try {
            SalesStages salesStages = this.salesStagesRepository.findById(id).orElseThrow(() -> new RuntimeException("Sales Stage not found"));
            SalesStagesDto salesStagesDto = new SalesStagesDto();
            salesStagesDto.setId(salesStages.getId());
            salesStagesDto.setSalesforceStageId(salesStages.getSalesforceStageId());
            salesStagesDto.setCrmId(salesStages.getCrm().getCrmId());
            salesStagesDto.setShortName(salesStages.getShortName());
            salesStagesDto.setDescription(salesStages.getDescription());
            return salesStagesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SalesStagesDto createSalesStage(SalesStagesDto salesStagesDto) {
        try {
            SalesStages salesStages = new SalesStages();
            CRM crm = crmRepository.findById(salesStagesDto.getCrmId()).orElseThrow(() -> new RuntimeException("CRM not found"));
            salesStages.setCrm(crm);
            salesStages.setSalesforceStageId(salesStagesDto.getSalesforceStageId());
            salesStages.setShortName(salesStagesDto.getShortName());
            salesStages.setDescription(salesStagesDto.getDescription());
            this.salesStagesRepository.save(salesStages);
            return salesStagesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SalesStagesDto updateSalesStage(Integer id, SalesStagesDto salesStagesDto) {
        try {
            SalesStages salesStages = this.salesStagesRepository.findById(id).orElseThrow(() -> new RuntimeException("Sales Stage not found"));
            CRM crm = crmRepository.findById(salesStagesDto.getCrmId()).orElseThrow(() -> new RuntimeException("CRM not found"));
            salesStages.setCrm(crm);
            salesStages.setSalesforceStageId(salesStagesDto.getSalesforceStageId());
            salesStages.setShortName(salesStagesDto.getShortName());
            salesStages.setDescription(salesStagesDto.getDescription());
            this.salesStagesRepository.save(salesStages);
            return salesStagesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSalesStage(Integer id) {
        try {
            SalesStages salesStages = this.salesStagesRepository.findById(id).orElseThrow(() -> new RuntimeException("Sales Stage not found"));
            this.salesStagesRepository.delete(salesStages);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
