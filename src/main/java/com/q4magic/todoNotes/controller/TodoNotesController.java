package com.q4magic.todoNotes.controller;

import com.q4magic.auth.config.JwtTokenUtil;
import com.q4magic.common.dto.TodoNotesDto;
import com.q4magic.common.response.ApiResponse;
import com.q4magic.todoNotes.service.TodoNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/todoNote")
public class TodoNotesController {
    @Autowired
    private JwtTokenUtil jwtUtil;

    @Autowired
    private TodoNotesService todoNotesService;

    @GetMapping("/getByTodo/{id}")
    public ApiResponse<?> findByTodoId(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Todo notes fetched successfully", this.todoNotesService.findByTodoId(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @GetMapping("/get/{id}")
    public ApiResponse<?> findById(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Todo notes fetched successfully", this.todoNotesService.findById(id));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PostMapping("/create")
    public ApiResponse<?> createTodo(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody TodoNotesDto todoNotesDto
            ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            Integer userId = jwtUtil.extractUserId(authorizationHeader.substring(7));
            return new ApiResponse<>(HttpStatus.CREATED.value(), "Todo notes saved successfully", this.todoNotesService.createTodo(userId,todoNotesDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @PatchMapping("/update/{id}")
    public ApiResponse<?> updateTodo(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @RequestBody TodoNotesDto todoNotesDto,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            return new ApiResponse<>(HttpStatus.OK.value(), "Todo notes updated successfully", this.todoNotesService.updateTodo(id,todoNotesDto));
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<?> deleteTodo(
            @RequestHeader(value = "Authorization") String authorizationHeader,
            @PathVariable("id") Integer id
    ) {
        Map<String, Object> resBody = new HashMap<>();
        try {
            this.todoNotesService.deleteTodo(id);
            return new ApiResponse<>(HttpStatus.OK.value(), "Todo notes deleted successfully", "");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage(), resBody);
        }
    }
}
