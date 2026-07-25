package com.q4magic.common.dto;

import lombok.Data;

@Data
public class NotesDto {
    private Integer id;
    private Integer meetingId;
    private String purpose;
    private String background;
    private String alignment;
    private String agenda;
}
