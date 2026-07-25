package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class ContactNodeDto {
    public Integer id;
    public String name;
    public String title;
    public List<ContactNodeDto> children; // null when leaf

}
