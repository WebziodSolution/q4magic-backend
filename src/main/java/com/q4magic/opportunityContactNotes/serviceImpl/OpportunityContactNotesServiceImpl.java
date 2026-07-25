package com.q4magic.opportunityContactNotes.serviceImpl;

import com.q4magic.common.dto.OpportunityContactNotesDto;
import com.q4magic.common.models.OpportunityContact;
import com.q4magic.common.models.OpportunityContactNotes;
import com.q4magic.common.repository.OpportunityContactNotesRepository;
import com.q4magic.common.repository.OpportunityContactRepository;
import com.q4magic.opportunityContactNotes.service.OpportunityContactNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "opportunitiesContactNote")
public class OpportunityContactNotesServiceImpl implements OpportunityContactNotesService {

    @Autowired
    private OpportunityContactNotesRepository opportunityContactNotesRepository;

    @Autowired
    private OpportunityContactRepository opportunityContactRepository;

    @Override
    public List<OpportunityContactNotesDto> findByOpportunityContactId(Integer opportunityContactId) {
        try {
            List<OpportunityContactNotes> opportunityContactNotesList = this.opportunityContactNotesRepository.findByContactId(opportunityContactId);
            List<OpportunityContactNotesDto> opportunityContactNotesDtoList = new ArrayList<>();
            if(!opportunityContactNotesList.isEmpty()){
                for (OpportunityContactNotes opportunityContactNotes : opportunityContactNotesList) {
                    opportunityContactNotesDtoList.add(this.getById(opportunityContactNotes.getId()));
                }
            }
            return opportunityContactNotesDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public OpportunityContactNotesDto getById(Integer id) {
        try {
            OpportunityContactNotes opportunityContactNotes = this.opportunityContactNotesRepository.findById(id).orElseThrow(() -> new RuntimeException("Notes not found"));
            OpportunityContactNotesDto opportunityContactNotesDto = new OpportunityContactNotesDto();
            opportunityContactNotesDto.setId(opportunityContactNotes.getId());
            opportunityContactNotesDto.setOpportunityContactId(opportunityContactNotes.getOpportunitiesContact().getId());
            opportunityContactNotesDto.setType(opportunityContactNotes.getType());
            opportunityContactNotesDto.setNote(opportunityContactNotes.getNote());
            return opportunityContactNotesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createOrUpdateOppContactNotes(List<OpportunityContactNotesDto> opportunityContactNotesDtos) {
        try {
            for (OpportunityContactNotesDto opportunityContactNotesDto : opportunityContactNotesDtos) {
                OpportunityContactNotes opportunityContactNotes = opportunityContactNotesDto.getId() != null ? this.opportunityContactNotesRepository.findById(opportunityContactNotesDto.getId()).orElseThrow(() -> new RuntimeException("Notes not found")) : new OpportunityContactNotes();
                OpportunityContact opportunityContact = this.opportunityContactRepository.findById(opportunityContactNotesDto.getOpportunityContactId()).orElseThrow(() -> new RuntimeException("Contact not found"));
                opportunityContactNotes.setOpportunitiesContact(opportunityContact);
                opportunityContactNotes.setType(opportunityContactNotesDto.getType());
                opportunityContactNotes.setNote(opportunityContactNotesDto.getNote());
                this.opportunityContactNotesRepository.save(opportunityContactNotes);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteOppContactNotes(Integer id) {
        try {
            OpportunityContactNotes opportunityContactNotes = this.opportunityContactNotesRepository.findById(id).orElseThrow(() -> new RuntimeException("Notes not found"));
            this.opportunityContactNotesRepository.delete(opportunityContactNotes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
