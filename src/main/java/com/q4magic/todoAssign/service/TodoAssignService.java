package com.q4magic.todoAssign.service;

import com.q4magic.common.dto.TodoAssignDto;

import java.util.List;

public interface TodoAssignService {
    List<TodoAssignDto> getAllTodosAssignToMe(Integer id);

    TodoAssignDto getAllByTodoId(Integer todoId, Integer loginUserId);

    List<TodoAssignDto> getAllAssignedTodos(Integer createdBy);

    TodoAssignDto getAssignTodoById(Integer id, Integer userId);

    TodoAssignDto assignTodoToUser(TodoAssignDto todoAssignDto);

//    TodoAssignDto updateAssignTodo(Integer id, TodoAssignDto todoAssignDto, Integer loginUserId);

    void deleteAssignTodo(Integer id);

    void setStatusToCompleted(Integer id);

    void sendTaskReminder(Integer userId, Integer todoId,Integer assignId);

//    TodoAssignTeamDto getAssignTodoTeamById(Integer id);
//
//    TodoAssignTeamDto createAssignTodoTeam(TodoAssignTeamDto todoAssignTeamDto);
//
//    TodoAssignTeamDto updateAssignTodoTeam(Integer id, TodoAssignTeamDto todoAssignTeamDto);

}
