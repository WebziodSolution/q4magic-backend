package com.q4magic.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class TodoAssignDto {
    private Integer id;
    private Integer todoId;
    private Integer customerId;
    private List<Integer> customerIds;
    private Integer teamId;
    private Integer assignBy;
    private Boolean isToday;
    private Integer complectedWork;
    private List<Integer> removeCustomerIds;
    private Integer removeTeam;
    private String status;
    private String dueDate;
    private String priority;
}
