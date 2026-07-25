package com.q4magic.common.salesforce.todo.controller;

import com.q4magic.common.response.ApiResponse;
import com.q4magic.common.salesforce.todo.service.SalesforceToDoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/salesforce/todo")
public class SalesforceToDoController {

    @Autowired
    private SalesforceToDoService salesforceToDoService;

    @GetMapping("/get/all")
    public ApiResponse<Map<String, Object>> getAllTasks(
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        return new ApiResponse<>(HttpStatus.OK.value(), "All tasks fetched successfully",
                salesforceToDoService.getAllTasks(accessToken, instanceUrl));
    }

    @GetMapping("/get/{id}")
    public ApiResponse<Map<String, Object>> getTaskById(
            @PathVariable("id") String taskId,
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Task fetched successfully",
                salesforceToDoService.getTaskById(taskId, accessToken, instanceUrl));
    }

    @PostMapping("/create")
    public ApiResponse<Map<String, Object>> createTask(
            @RequestBody Map<String, Object> taskData,
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Task created successfully",
                salesforceToDoService.createTask(taskData, accessToken, instanceUrl));
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<Map<String, Object>> updateTask(
            @PathVariable("id") String taskId,
            @RequestBody Map<String, Object> taskData,
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Task updated successfully",
                salesforceToDoService.updateTask(taskId, taskData, accessToken, instanceUrl));
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Map<String, Object>> deleteTask(
            @PathVariable("id") String taskId,
            @RequestParam("access_token") String accessToken,
            @RequestParam("instance_url") String instanceUrl) {
        return new ApiResponse<>(HttpStatus.OK.value(), "Task deleted successfully",
                salesforceToDoService.deleteTask(taskId, accessToken, instanceUrl));
    }
}