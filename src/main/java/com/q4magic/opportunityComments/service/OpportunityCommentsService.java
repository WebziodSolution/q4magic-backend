package com.q4magic.opportunityComments.service;

import com.q4magic.common.dto.OpportunityCommentsDto;

import java.util.List;

public interface OpportunityCommentsService {
    List<OpportunityCommentsDto> getCommentsByoOppId(Integer oppId, Integer cusId);

    OpportunityCommentsDto getComments(Integer id);

    OpportunityCommentsDto saveComments(Integer cusId, OpportunityCommentsDto opportunityCommentsDto);

    OpportunityCommentsDto updateComments(Integer id, OpportunityCommentsDto opportunityCommentsDto);

    void deleteComments(Integer id);

}

