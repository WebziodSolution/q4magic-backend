package com.q4magic.closePlanNotes.service;

import com.q4magic.common.dto.ClosePlanNotesDto;

import java.util.List;

public interface ClosePlanNotesService {
    List<ClosePlanNotesDto> findLastTwoComments(Integer closePlanId, Integer contactId);

    List<ClosePlanNotesDto> findByClosePlanIdAndContactId(Integer closePlanId, Integer contactId);

    ClosePlanNotesDto saveComment(ClosePlanNotesDto closePlanNotesDto);
}
