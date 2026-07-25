package com.q4magic.common.dto;

import lombok.Data;

@Data
public class DocsAttachmentsDto {
    private Integer id;
    private Integer categoryId;
    private String type;
    private String linkName;
    private String link;
    private String fileName;
    private String imageName;
    private String fileUrl;
}
