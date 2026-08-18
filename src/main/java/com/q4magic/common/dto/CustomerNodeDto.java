package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class CustomerNodeDto {
    public Integer id;
    public String name;
    public String title;
    public List<CustomerNodeDto> children; // null when leaf
}
