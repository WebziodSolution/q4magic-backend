package com.q4magic.salesProcess.serviceImpl;

import com.q4magic.common.dto.SalesProcessDto;
import com.q4magic.common.models.Contacts;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.OpportunityContact;
import com.q4magic.common.models.SalesProcess;
import com.q4magic.common.repository.ContactsRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.OpportunityContactRepository;
import com.q4magic.common.repository.SalesProcessRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.salesProcess.service.SalesProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("SalesProcessService")
public class SalesProcessServiceImpl implements SalesProcessService {

    @Autowired
    private SalesProcessRepository salesProcessRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private ContactsRepository contactsRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Override
    public List<SalesProcessDto> getAllByOpportunity(Integer opportunityId) {
        try {
            List<SalesProcess> salesProcessList = this.salesProcessRepository.findByOppId(opportunityId);
            List<SalesProcessDto> salesProcessDtoList = new ArrayList<>();
            if (!salesProcessList.isEmpty()) {
                for (SalesProcess salesProcess : salesProcessList) {
                    salesProcessDtoList.add(this.getSaleProcess(salesProcess.getId()));
                }
            }
            return salesProcessDtoList;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public SalesProcessDto getSaleProcess(Integer id) {
        try {
            SalesProcess salesProcess = this.salesProcessRepository.findById(id).orElseThrow(() -> new RuntimeException("Sales process not found"));
            SalesProcessDto salesProcessDto = new SalesProcessDto();
            if (salesProcess.getContacts() != null) {
                salesProcessDto.setContactId(salesProcess.getContacts().getId());
            }

            if (salesProcess.getProcessDate() != null) {
                salesProcessDto.setProcessDate(this.commonService.convertDateToString(salesProcess.getProcessDate()));
            }

            if (salesProcess.getGoLive() != null) {
                salesProcessDto.setGoLive(this.commonService.convertDateToString(salesProcess.getGoLive()));
            }
            salesProcessDto.setProcess(salesProcess.getProcess());
            salesProcessDto.setNotes(salesProcess.getNotes());
            salesProcessDto.setId(salesProcess.getId());
            salesProcessDto.setReason(salesProcess.getReason());
            return salesProcessDto;
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public SalesProcessDto createSaleProcess(SalesProcessDto salesProcessDto) {
        try {
            SalesProcess salesProcess = new SalesProcess();
            Opportunities opportunities = this.opportunitiesRepository.findById(salesProcessDto.getOppId()).orElseThrow(() -> new RuntimeException("Opp not found"));
            salesProcess.setOpportunities(opportunities);

            if (salesProcessDto.getContactId() != null) {
                OpportunityContact opportunityContact = this.opportunityContactRepository.findById(salesProcessDto.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
                salesProcess.setContacts(opportunityContact);
            }

            if (salesProcessDto.getProcessDate() != null) {
                salesProcess.setProcessDate(this.commonService.convertStringToDate(salesProcessDto.getProcessDate()));
            }

            if (salesProcessDto.getGoLive() != null) {
                salesProcess.setGoLive(this.commonService.convertStringToDate(salesProcessDto.getGoLive()));
            }

            BeanUtils.copyProperties(salesProcessDto, salesProcess, "goLive", "processDate", "oppId", "contactId", "id");

            this.salesProcessRepository.save(salesProcess);
            salesProcessDto.setId(salesProcess.getId());
            return salesProcessDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public SalesProcessDto updateSaleProcess(Integer id, SalesProcessDto salesProcessDto) {
        try {
            SalesProcess salesProcess = this.salesProcessRepository.findById(id).orElseThrow(() -> new RuntimeException("Sales process not found"));
            Opportunities opportunities = this.opportunitiesRepository.findById(salesProcessDto.getOppId()).orElseThrow(() -> new RuntimeException("Opp not found"));
            salesProcess.setOpportunities(opportunities);

            if (salesProcessDto.getContactId() != null) {
                OpportunityContact opportunityContact = this.opportunityContactRepository.findById(salesProcessDto.getContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
                salesProcess.setContacts(opportunityContact);
            }

            if (salesProcessDto.getProcessDate() != null) {
                salesProcess.setProcessDate(this.commonService.convertStringToDate(salesProcessDto.getProcessDate()));
            }

            if (salesProcessDto.getGoLive() != null) {
                salesProcess.setGoLive(this.commonService.convertStringToDate(salesProcessDto.getGoLive()));
            }
            BeanUtils.copyProperties(salesProcessDto, salesProcess, "goLive", "processDate", "oppId", "contactId", "id");
            this.salesProcessRepository.save(salesProcess);
            salesProcessDto.setId(salesProcess.getId());
            return salesProcessDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteSaleProcess(Integer id) {
        try {
            SalesProcess salesProcess = this.salesProcessRepository.findById(id).orElseThrow(() -> new RuntimeException("Sales process not found"));
            this.salesProcessRepository.delete(salesProcess);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
