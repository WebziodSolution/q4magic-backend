package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class LocationResultDto {
    private String status;
    private List<String> extractedLocations;
}
