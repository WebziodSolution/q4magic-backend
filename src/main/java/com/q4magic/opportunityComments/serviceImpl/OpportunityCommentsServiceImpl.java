package com.q4magic.opportunityComments.serviceImpl;

import com.q4magic.common.dto.OpportunityCommentsDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Opportunities;
import com.q4magic.common.models.OpportunityComments;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.repository.OpportunityCommentsRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.opportunityComments.service.OpportunityCommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(value = "OpportunityCommentsService")
public class OpportunityCommentsServiceImpl implements OpportunityCommentsService {

    @Autowired
    private OpportunityCommentsRepository opportunityCommentsRepository;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<OpportunityCommentsDto> getCommentsByoOppId(Integer oppId, Integer cusId) {
        try {
            List<OpportunityComments> opportunityCommentsList = this.opportunityCommentsRepository.findByOppIdAndCustomerId(cusId, oppId);
            List<OpportunityCommentsDto> opportunityCommentsDtoList = new ArrayList<>();
            if (!opportunityCommentsList.isEmpty()) {
                for (OpportunityComments opportunityComments : opportunityCommentsList) {
                    opportunityCommentsDtoList.add(this.getComments(opportunityComments.getId()));
                }
            }
            return opportunityCommentsDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityCommentsDto getComments(Integer id) {
        try {
            OpportunityComments opportunityComments = this.opportunityCommentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Opportunity comments not found"));
            OpportunityCommentsDto opportunityCommentsDto = new OpportunityCommentsDto();
            opportunityCommentsDto.setId(opportunityComments.getId());
            opportunityCommentsDto.setTitle(opportunityComments.getTitle());
            opportunityCommentsDto.setComment(opportunityComments.getComment());
            opportunityCommentsDto.setOppId(opportunityComments.getOpportunities().getId());
            opportunityCommentsDto.setCusId(opportunityComments.getCustomers().getId());
            opportunityCommentsDto.setCommentDate(this.commonService.convertDateToString(opportunityComments.getCommentDate()));
            return opportunityCommentsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityCommentsDto saveComments(Integer cusId, OpportunityCommentsDto opportunityCommentsDto) {
        try {
            Opportunities opportunities = this.opportunitiesRepository.findById(opportunityCommentsDto.getOppId()).orElseThrow(() -> new RuntimeException("Opp not found"));
            Customers customers = this.customersRepository.findById(cusId).orElseThrow(() -> new RuntimeException("Customer not found"));

            OpportunityComments opportunityComments = new OpportunityComments();
            opportunityComments.setCustomers(customers);
            opportunityComments.setOpportunities(opportunities);
            opportunityComments.setTitle(opportunityCommentsDto.getTitle());
            opportunityComments.setComment(opportunityCommentsDto.getComment());
            opportunityComments.setCommentDate(new Date());
            this.opportunityCommentsRepository.save(opportunityComments);
            return opportunityCommentsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityCommentsDto updateComments(Integer id, OpportunityCommentsDto opportunityCommentsDto) {
        try {
            OpportunityComments opportunityComments = this.opportunityCommentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Opportunity comments not found"));
            opportunityComments.setTitle(opportunityCommentsDto.getTitle());
            opportunityComments.setComment(opportunityCommentsDto.getComment());
            this.opportunityCommentsRepository.save(opportunityComments);
            return opportunityCommentsDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteComments(Integer id) {
        try {
            OpportunityComments opportunityComments = this.opportunityCommentsRepository.findById(id).orElseThrow(() -> new RuntimeException("Opportunity comments not found"));
            this.opportunityCommentsRepository.delete(opportunityComments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

