package com.q4magic.todo.controller;


import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.TodoDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.todo.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/todo")
public class TodoController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private TodoService todoService;

    @PostMapping("/getTodoByTeam")
    public ApiResponse<?> getTodoByTeam(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody Map<String, Object> requestBody
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            List<Integer> teamIds = (List<Integer>) requestBody.get("teamIds");
            String status = (String) requestBody.get("status");

            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos fetched successfully", this.todoService.getTodoByTeam(userId, teamIds,status));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/get/all")
    public ApiResponse<?> getAllTodos(
            @RequestHeader(value = "Authorization") String authorizationHeader
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos fetched successfully", this.todoService.getAllTodo(userId));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch todos", resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> getTodo(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos fetched successfully", this.todoService.getTodoById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to fetch todos", resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createTodo(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody TodoDto todoDto
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            todoDto.setCreatedBy(userId);

            return new ApiResponse<>(HttpStatus.CREATED.value(), "Todos created successfully", this.todoService.createTodo(todoDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to create todos", resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateTodo(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody TodoDto todoDto,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos updated successfully", this.todoService.updateTodo(id, todoDto, true));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update todos", resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deletedTodo(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoService.deleteTodo(id, true);
            return new ApiResponse<>(HttpStatus.OK.value(), "Todos deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to delete todos", resBody);
        }
    }

    @DeleteMapping("/image/{imageId}")
    public ApiResponse<?> deleteImagesById(@RequestHeader(value = "Authorization", required = true) String authorizationHeader, @PathVariable Integer imageId) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoService.deleteImagesById(imageId);
            return new ApiResponse<>(HttpStatus.OK.value(), "Image deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/completeTodo/{id}")
    public ApiResponse<?> completeTodo(@RequestHeader(value = "Authorization", required = true) String authorizationHeader, @PathVariable Integer id) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoService.completeTodo(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Task closed successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }
}
