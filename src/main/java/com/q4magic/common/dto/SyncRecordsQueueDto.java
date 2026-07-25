package com.q4magic.common.dto;

import lombok.Data;

@Data
public class SyncRecordsQueueDto {
    private Integer id;
    private Integer subjectId;
    private String subject;
    private String operationType;
    private String syncType;
    private String error;
    private String date;
    private Integer createdBy;
    private String createdByName;
    private boolean isDeleted;
}
