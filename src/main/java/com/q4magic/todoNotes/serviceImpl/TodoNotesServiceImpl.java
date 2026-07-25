package com.q4magic.todoNotes.serviceImpl;

import com.q4magic.common.dto.TodoNotesDto;
import com.q4magic.common.models.Customers;
import com.q4magic.common.models.Todo;
import com.q4magic.common.models.TodoNotes;
import com.q4magic.common.repository.CustomersRepository;
import com.q4magic.common.repository.TodoNotesRepository;
import com.q4magic.common.repository.TodoRepository;
import com.q4magic.common.service.CommonService;
import com.q4magic.todoNotes.service.TodoNotesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service(value = "TodoNotesService")
public class TodoNotesServiceImpl implements TodoNotesService {

    @Autowired
    private TodoNotesRepository todoNotesRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<TodoNotesDto> findByCustomer(Integer cusId) {
        try {
            List<TodoNotes> todoNotesList = this.todoNotesRepository.getByCustomer(cusId);
            List<TodoNotesDto> todoNotesDtoList = new ArrayList<>();
            if(!todoNotesList.isEmpty()){
                for (TodoNotes todoNotes : todoNotesList) {
                    todoNotesDtoList.add(this.findById(todoNotes.getId()));
                }
            }
            return todoNotesDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TodoNotesDto> findByTodoId(Integer todoId) {
        try {
            List<TodoNotes> todoNotesList = this.todoNotesRepository.getByTodoId(todoId);
            List<TodoNotesDto> todoNotesDtoList = new ArrayList<>();
            if(!todoNotesList.isEmpty()){
                for (TodoNotes todoNotes : todoNotesList) {
                    todoNotesDtoList.add(this.findById(todoNotes.getId()));
                }
            }
            return todoNotesDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoNotesDto findById(Integer id) {
        try {
            TodoNotes todoNotes = this.todoNotesRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
            TodoNotesDto todoNotesDto = new TodoNotesDto();
            todoNotesDto.setId(todoNotes.getId());
            todoNotesDto.setNote(todoNotes.getNote());
            todoNotesDto.setCustomerId(todoNotes.getCustomers().getId());
            todoNotesDto.setTodoId(todoNotes.getTodo().getId());
            todoNotesDto.setCreatedAt(this.commonService.convertDateToString(todoNotes.getCreatedAt()));
            return todoNotesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoNotesDto createTodo(Integer cusId, TodoNotesDto todoNotesDto) {
        try {
            TodoNotes todoNotes = new TodoNotes();
            Customers customers = this.customersRepository.findById(cusId).orElseThrow(() -> new Exception("Customer not found"));
            Todo todo = this.todoRepository.findById(todoNotesDto.getTodoId()).orElseThrow(() -> new RuntimeException("Todo not found"));

            todoNotes.setCustomers(customers);
            todoNotes.setTodo(todo);
            todoNotes.setNote(todoNotesDto.getNote());
            todoNotes.setCreatedAt(new Date());
            this.todoNotesRepository.save(todoNotes);
            todoNotesDto.setId(todoNotes.getId());
            return todoNotesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoNotesDto updateTodo(Integer id, TodoNotesDto todoNotesDto) {
        try {
            TodoNotes todoNotes = this.todoNotesRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
            todoNotes.setNote(todoNotesDto.getNote());
            this.todoNotesRepository.save(todoNotes);
            return todoNotesDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTodo(Integer id) {
        try {
            TodoNotes todoNotes = this.todoNotesRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));
            this.todoNotesRepository.delete(todoNotes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
