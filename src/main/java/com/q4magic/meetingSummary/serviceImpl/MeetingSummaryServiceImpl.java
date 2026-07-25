package com.q4magic.meetingSummary.serviceImpl;

import com.q4magic.common.dto.MeetingSummaryDto;
import com.q4magic.common.models.MeetingSummary;
import com.q4magic.common.repository.MeetingSummaryRepository;
import com.q4magic.common.repository.OpportunitiesRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.meetingSummary.service.MeetingSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service(value = "MeetingSummaryService")
public class MeetingSummaryServiceImpl implements MeetingSummaryService {

    @Autowired
    private MeetingSummaryRepository meetingSummaryRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<MeetingSummaryDto> getMeetingSummaryByOppId(Integer opportunityId, Integer cusId) {
        try {
            List<MeetingSummaryDto> meetingSummaryDtoList = new ArrayList<>();
            List<MeetingSummary> meetingSummaryList = this.meetingSummaryRepository.findByOppIdAndCusId(cusId, opportunityId);
            if (meetingSummaryList != null) {
                for (MeetingSummary meetingSummary : meetingSummaryList) {
                    MeetingSummaryDto meetingSummaryDto = new MeetingSummaryDto();
                    meetingSummaryDto.setId(meetingSummary.getId());
                    meetingSummaryDto.setSummary(meetingSummary.getSummary());
                    meetingSummaryDto.setTranscript(meetingSummary.getTranscript());
                    meetingSummaryDto.setCusId(cusId);
                    meetingSummaryDto.setOpportunityId(opportunityId);
                    meetingSummaryDto.setOppName(meetingSummary.getOpportunities().getOpportunity());
                    meetingSummaryDto.setDate(this.commonService.convertDateToString(meetingSummary.getCreatedDate()));
                    meetingSummaryDtoList.add(meetingSummaryDto);
                }
            }
            return meetingSummaryDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
