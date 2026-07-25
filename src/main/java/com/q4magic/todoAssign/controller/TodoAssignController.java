package com.q4magic.todoAssign.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.TodoAssignDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.todoAssign.service.TodoAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/todoAssign")
public class TodoAssignController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private TodoAssignService todoAssignService;

    @GetMapping("/setStatusToCompleted/{id}")
    public ApiResponse<?> setStatusToCompleted(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoAssignService.setStatusToCompleted(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Todo status updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/getAllAssignToMe")
    public ApiResponse<?> getAllTodosAssignToMe(
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos fetched successfully", this.todoAssignService.getAllTodosAssignToMe(userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<?> getAllTodoAssign(
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos fetched successfully", this.todoAssignService.getAllAssignedTodos(userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/getByTodo/{id}")
    public ApiResponse<?> getByTodoId(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos fetched successfully", this.todoAssignService.getAllByTodoId(id,userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getTodoAssign(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos fetched successfully", this.todoAssignService.getAssignTodoById(id,userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createTodoAssign(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody TodoAssignDto todoAssignDto
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            todoAssignDto.setAssignBy(userId);
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Todos created successfully", this.todoAssignService.assignTodoToUser(todoAssignDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteTodoAssign(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoAssignService.deleteAssignTodo(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos updated successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch todos", resBody);
        }
    }

    @GetMapping("/sendTaskReminder/{userId}/{todoId}/{assignId}")
    public ApiResponse<?> sendTaskReminder(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("userId") Integer userId,
            @PathVariable("todoId") Integer todoId,
            @PathVariable("assignId") Integer assignId
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoAssignService.sendTaskReminder(userId,todoId,assignId);
            return new ApiResponse<>(HttpStatus.OK.value(), "E-Mail send successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }
}
