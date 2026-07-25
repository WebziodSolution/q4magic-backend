package com.q4magic.common.dto;

import lombok.Data;

@Data
public class TodoNotesDto {
    private Integer id;
    private String note;
    private String createdAt;
    private Integer todoId;
    private Integer customerId;
}
