package com.q4magic.processName.serviceImpl;

import com.q4magic.common.dto.ProcessNameDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.ProcessName;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.ProcessNameRepository;
import com.q4magic.processName.service.ProcessNameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "ProcessNameService")
public class ProcessNameServiceImpl implements ProcessNameService {

    @Autowired
    private ProcessNameRepository processNameRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Override
    public List<ProcessNameDto> getAllByOpportunity(Integer oppId) {
        try {
            List<ProcessName> entities =
                    this.processNameRepository.findByOppId(oppId);

            List<ProcessNameDto> result = new ArrayList<>();
            if (entities != null && !entities.isEmpty()) {
                for (ProcessName p : entities) {
                    result.add(this.getProcessName(p.getId()));
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ProcessNameDto> getAllByCustomer(Integer createdBy) {
        try {
            List<ProcessName> entities =
                    this.processNameRepository.findByContactId(createdBy);

            List<ProcessNameDto> result = new ArrayList<>();
            if (entities != null && !entities.isEmpty()) {
                for (ProcessName p : entities) {
                    result.add(this.getProcessName(p.getId()));
                }
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProcessNameDto getProcessName(Integer id) {
        try {
            ProcessName entity = this.processNameRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("ProcessName not found"));
            ProcessNameDto processNameDto = new ProcessNameDto();

            if (entity.getOpportunities() != null) {
                processNameDto.setOppId(entity.getOpportunities().getId());
            }
            processNameDto.setId(entity.getId());
            processNameDto.setName(entity.getName());
            processNameDto.setCreatedBy(entity.getCustomers().getId());
            return processNameDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProcessNameDto createProcessName(ProcessNameDto processNameDto) {
        try {
            ProcessName entity = new ProcessName();

            if (processNameDto.getOppId() != null) {
                Opportunities opp = this.opportunitiesRepository.findById(processNameDto.getOppId())
                        .orElseThrow(() -> new RuntimeException("Opportunity not found"));
                entity.setOpportunities(opp);
            }

            if (processNameDto.getCreatedBy() != null) {
                Customers customer = this.customersRepository.findById(processNameDto.getCreatedBy())
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                entity.setCustomers(customer);
            }

            entity.setName(processNameDto.getName());

            this.processNameRepository.save(entity);
            processNameDto.setId(entity.getId());
            return processNameDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public ProcessNameDto updateProcessName(Integer id, ProcessNameDto processNameDto) {
        try {
            ProcessName entity = this.processNameRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("ProcessName not found"));

            // update relations if provided
            if (processNameDto.getOppId() != null) {
                Opportunities opp = this.opportunitiesRepository.findById(processNameDto.getOppId())
                        .orElseThrow(() -> new RuntimeException("Opportunity not found"));
                entity.setOpportunities(opp);
            }

            if (processNameDto.getCreatedBy() != null) {
                Customers customer = this.customersRepository.findById(processNameDto.getCreatedBy())
                        .orElseThrow(() -> new RuntimeException("Customer not found"));
                entity.setCustomers(customer);
            }

            entity.setName(processNameDto.getName());

            this.processNameRepository.save(entity);

            return processNameDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteProcessName(Integer id) {
        try {
            ProcessName entity = this.processNameRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("ProcessName not found"));
            this.processNameRepository.delete(entity);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
