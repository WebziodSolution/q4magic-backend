package com.q4magic.opportunitiesCurrentEnvironment.serviceImpl;

import com.q4magic.common.dto.OpportunitiesCurrentEnvironmentDto;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.OpportunitiesCurrentEnvironment;
import com.q4magic.common.repository.OpportunitiesCurrentEnvironmentRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.opportunitiesCurrentEnvironment.service.OpportunitiesCurrentEnvironmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "OpportunitiesCurrentEnvironmentService")
public class OpportunitiesCurrentEnvironmentServiceImpl implements OpportunitiesCurrentEnvironmentService {

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private OpportunitiesCurrentEnvironmentRepository opportunitiesCurrentEnvironmentRepository;

    @Override
    public List<OpportunitiesCurrentEnvironmentDto> getOpportunitiesCurrentEnvironmentByOppId(Integer id) {
        try {
            List<OpportunitiesCurrentEnvironment> opportunitiesCurrentEnvironmentList = this.opportunitiesCurrentEnvironmentRepository.findByOppId(id);
            List<OpportunitiesCurrentEnvironmentDto> opportunitiesCurrentEnvironmentDtoList = new ArrayList<>();
            if (!opportunitiesCurrentEnvironmentList.isEmpty()) {
                for (OpportunitiesCurrentEnvironment opportunitiesCurrentEnvironment : opportunitiesCurrentEnvironmentList) {
                    opportunitiesCurrentEnvironmentDtoList.add(this.getOpportunitiesCurrentEnvironment(opportunitiesCurrentEnvironment.getId()));
                }
            }
            return opportunitiesCurrentEnvironmentDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunitiesCurrentEnvironmentDto getOpportunitiesCurrentEnvironment(Integer id) {
        try {
            OpportunitiesCurrentEnvironment opportunitiesCurrentEnvironment = this.opportunitiesCurrentEnvironmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Opportunity current env not found"));
            OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto = new OpportunitiesCurrentEnvironmentDto();
            opportunitiesCurrentEnvironmentDto.setId(opportunitiesCurrentEnvironment.getId());
            opportunitiesCurrentEnvironmentDto.setOppId(opportunitiesCurrentEnvironment.getOpportunities().getId());
            opportunitiesCurrentEnvironmentDto.setSolution(opportunitiesCurrentEnvironment.getSolution());
            opportunitiesCurrentEnvironmentDto.setVendors(opportunitiesCurrentEnvironment.getVendors());
            return opportunitiesCurrentEnvironmentDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunitiesCurrentEnvironmentDto addOpportunitiesCurrentEnvironment(OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto) {
        try {
            OpportunitiesCurrentEnvironment opportunitiesCurrentEnvironment = new OpportunitiesCurrentEnvironment();
            Opportunities opportunities = this.opportunitiesRepository.findById(opportunitiesCurrentEnvironmentDto.getOppId()).orElseThrow(() -> new RuntimeException("Opportunity not found"));
            opportunitiesCurrentEnvironment.setOpportunities(opportunities);
            BeanUtils.copyProperties(opportunitiesCurrentEnvironmentDto, opportunitiesCurrentEnvironment);
            this.opportunitiesCurrentEnvironmentRepository.save(opportunitiesCurrentEnvironment);
            opportunitiesCurrentEnvironmentDto.setId(opportunitiesCurrentEnvironment.getId());
            return opportunitiesCurrentEnvironmentDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunitiesCurrentEnvironmentDto updateOpportunitiesCurrentEnvironment(Integer id, OpportunitiesCurrentEnvironmentDto opportunitiesCurrentEnvironmentDto) {
        try {
            OpportunitiesCurrentEnvironment opportunitiesCurrentEnvironment = this.opportunitiesCurrentEnvironmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Opportunity current env not found"));
            Opportunities opportunities = this.opportunitiesRepository.findById(opportunitiesCurrentEnvironmentDto.getOppId()).orElseThrow(() -> new RuntimeException("Opportunity not found"));
            opportunitiesCurrentEnvironment.setOpportunities(opportunities);
            BeanUtils.copyProperties(opportunitiesCurrentEnvironmentDto, opportunitiesCurrentEnvironment);
            this.opportunitiesCurrentEnvironmentRepository.save(opportunitiesCurrentEnvironment);
            return opportunitiesCurrentEnvironmentDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOpportunitiesCurrentEnvironment(Integer id) {
        try {
            OpportunitiesCurrentEnvironment opportunitiesCurrentEnvironment = this.opportunitiesCurrentEnvironmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Opportunity current env not found"));
            this.opportunitiesCurrentEnvironmentRepository.delete(opportunitiesCurrentEnvironment);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
