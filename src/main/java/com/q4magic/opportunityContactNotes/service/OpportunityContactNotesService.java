package com.q4magic.opportunityContactNotes.service;

import com.q4magic.common.dto.OpportunityContactNotesDto;

import java.util.List;

public interface OpportunityContactNotesService {
    List<OpportunityContactNotesDto>  findByOpportunityContactId(Integer opportunityContactId);

    OpportunityContactNotesDto  getById(Integer id);

    void  createOrUpdateOppContactNotes(List<OpportunityContactNotesDto> opportunityContactNotesDtos);

    void  deleteOppContactNotes(Integer id);

}
