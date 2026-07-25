package com.q4magic.todoNotes.service;

import com.q4magic.common.dto.TodoNotesDto;
import java.util.List;

public interface TodoNotesService {
    List<TodoNotesDto> findByCustomer(Integer cusId);

    List<TodoNotesDto> findByTodoId(Integer todoId);

    TodoNotesDto findById(Integer id);

    TodoNotesDto createTodo(Integer cusId, TodoNotesDto todoNotesDto);

    TodoNotesDto updateTodo(Integer id, TodoNotesDto todoNotesDto);

    void deleteTodo(Integer id);
}
