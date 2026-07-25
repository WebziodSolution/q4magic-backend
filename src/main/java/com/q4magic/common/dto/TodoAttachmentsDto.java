package com.q4magic.common.dto;

import lombok.Data;

@Data
public class TodoAttachmentsDto {
    private Integer id;
    private Integer todoId;
    private String type;
    private String fileName;
    private String imageName;
    private String path;
    private String link;
    private String linkName;
}
