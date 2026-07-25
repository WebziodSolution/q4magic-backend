package com.q4magic.common.dto;

import lombok.Data;

@Data
public class FreeSlotListDto {
    private Integer slotTimeMinus;
    private String slotDateTime;
    private Integer slotUserId;
    private String timeZone;
    private String currentDateYN;
}
