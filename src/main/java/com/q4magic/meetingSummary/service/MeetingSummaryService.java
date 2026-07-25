package com.q4magic.meetingSummary.service;

import com.q4magic.common.dto.MeetingSummaryDto;

import java.util.List;

public interface MeetingSummaryService {
    List<MeetingSummaryDto> getMeetingSummaryByOppId(Integer opportunityId,Integer cusId);
}
