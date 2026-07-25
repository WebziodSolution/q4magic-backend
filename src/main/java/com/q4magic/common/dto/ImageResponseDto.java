package com.q4magic.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class ImageResponseDto {
    private Integer imageId;
    private String imageName;
    private String imageURL;
    private Boolean isInternal;
}
