package com.q4magic.todo.service;

import com.q4magic.common.dto.TodoDto;

import java.util.List;
import java.util.Map;

public interface TodoService {
    List<TodoDto> getTodoByTeam(Integer customerId, List<Integer> teamIds,String status);

    List<TodoDto> getAllTodo(Integer customerId);

    TodoDto getTodoById(Integer id);

    TodoDto createTodo(TodoDto todoDto, Boolean syncToSalesforce);

    TodoDto updateTodo(Integer id, TodoDto todoDto, Boolean syncToSalesforce);

    void deleteTodo(Integer id, Boolean syncToSalesforce);

    void deleteImagesById(Integer imageId);

    void completeTodo(Integer todoId);

}
