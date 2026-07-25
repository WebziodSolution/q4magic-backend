package com.q4magic.opportunityContact.service;

import com.q4magic.common.dto.OpportunityContactDto;

import java.util.List;

public interface OpportunityContactService {
    List<OpportunityContactDto> getAllOppContact(Integer oppId);

    void addOppContact(Integer createdById, List<OpportunityContactDto> opportunityContactDto, Boolean isSyncToSalesforce);

    void addOppContact(Integer createdById, OpportunityContactDto opportunityContactDto, Boolean isSyncToSalesforce);

    void updateOppContact(Integer createdById, List<OpportunityContactDto> opportunityContactDto, Boolean isSyncToSalesforce);

    void updateOppContact(Integer createdById, Integer id, OpportunityContactDto opportunityContactDto, Boolean isSyncToSalesforce);

    void deleteOppContact(Integer createdById, Integer id,Boolean isSyncToSalesforce);
}
