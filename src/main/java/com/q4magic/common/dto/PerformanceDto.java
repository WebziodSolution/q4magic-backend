package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PerformanceDto {
    private Integer id;
    private Integer totalMettings;
    private Integer newMettings;
    private Integer oldMettings;
    private Integer cusId;
    private Integer onsiteCount;
    private Integer virtualCount;
    private List<Map<String, Object>> data;
    private Integer hunterCount;
    private Integer farmerCount;
    private Integer meetingQuota;

}
