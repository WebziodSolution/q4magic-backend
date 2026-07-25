package com.q4magic.common.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class CustomerDashboardDto {
    private Integer customerId;
    private Integer totalContacts;
    private Integer totalPipeLine;
    private Integer totalMeetings;
    private Integer totalNewMeetings;
    private Integer totalOldMeetings;
    private Integer totalClosedDealAmount;
    private Integer totalDealAmount;
    private List<Map<String,Object>> pipeLineData;
    private List<Map<String,Object>> meetingData;
}
