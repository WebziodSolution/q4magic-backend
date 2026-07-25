package com.q4magic.common.dto;

import lombok.Data;

@Data
public class DeleteEventDto {
    private Integer calId;
    private Boolean googleCalendar;
    private Boolean outlookCalendar;
    private Integer calParentId;
    private String deleteAll;
}
