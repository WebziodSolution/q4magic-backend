package com.q4magic.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class TodoDto {
    private Integer id;
    private String relatedTo;

    private String task;
    private String description;
    private String dueDate;
    private Boolean isDeleted;
    private Integer createdBy;
    private String createdByName;
    private String teamName;

    private Integer complectedWork;
    private String customId;
    private String priority;
    private List<TodoAttachmentsDto> todoAttachmentsDtos = new ArrayList<>();
    private List<Map<String,Object>> todoAssignData = new ArrayList<>();
    private List<ImageResponseDto> images = new ArrayList<>();

}
