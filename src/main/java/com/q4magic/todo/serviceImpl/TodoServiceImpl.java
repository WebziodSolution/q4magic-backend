package com.q4magic.todo.serviceImpl;

import com.q4magic.common.dto.*;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.common.service.CommonService;
import com.q4magic.common.specification.TodoSpecification;
import com.q4magic.todo.service.TodoService;
import com.q4magic.todoAssign.service.TodoAssignService;
import com.q4magic.todoAttachments.service.TodoAttachmentsService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

@Service(value = "TodoService")
public class TodoServiceImpl implements TodoService {

    @Value("${360pipeDrive}")
    String FILE_DIRECTORY;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private CommonService commonService;

    @Autowired
    private OpportunitiesRepository opportunitiesRepository;

    @Autowired
    private TodoAssignService todoAssignService;

    @Autowired
    private TodoAssignRepository todoAssignRepository;

    @Autowired
    private TodoAttachmentsRepository todoAttachmentsRepository;

    @Autowired
    private TodoAttachmentsService todoAttachmentsService;

    @Override
    public List<TodoDto> getTodoByTeam(Integer customerId, List<Integer> teamIds, String status) {
        try {

            Specification<TodoAssign> spec = Specification.anyOf(
                    TodoSpecification.createdByMe(customerId),
                    TodoSpecification.assignedToMe(customerId)
            ).and(
                    Specification.allOf(
                            TodoSpecification.teamIdIn(teamIds),
                            TodoSpecification.hasStatus(status)
                    )
            );

            // Define sorting: Order by teamDetails.id in Descending order
            Sort sort = Sort.by(Sort.Direction.DESC, "todo.id");

            // Fetch using the Specification and Sort
            List<TodoAssign> assignments = todoAssignRepository.findAll(spec, sort);

            return assignments.stream()
                    .map(assignment -> assignment.getTodo().getId())
                    .distinct()
                    .map(this::getTodoById)
                    .toList();

        } catch (Exception e) {
            throw new RuntimeException("Error filtering todos", e);
        }
    }

    @Override
    public List<TodoDto> getAllTodo(Integer customerId) {
        try {
            List<Todo> todoList = this.todoRepository.findByCustomerId(customerId);
            List<TodoDto> todoDtoList = new ArrayList<>();
            for (Todo todo : todoList) {
                todoDtoList.add(this.getTodoById(todo.getId()));
            }
            return todoDtoList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoDto getTodoById(Integer id) {
        try {
            Todo todo = this.todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found"));
            TodoDto todoDto = new TodoDto();
            if (todo.getDueDate() != null) {
                todoDto.setDueDate(this.commonService.convertDateToString(todo.getDueDate()));
            }
            if (todo.getCreatedBy() != null) {
                todoDto.setCreatedBy(todo.getCreatedBy().getId());
                todoDto.setCreatedByName((todo.getCreatedBy().getUsername() != null && todo.getCreatedBy().getUsername().equalsIgnoreCase("")) ? todo.getCreatedBy().getUsername() : todo.getCreatedBy().getFirstName() + " " + todo.getCreatedBy().getLastName());

            }

            BeanUtils.copyProperties(todo, todoDto);
            List<TodoAttachments> images = this.todoAttachmentsRepository.getByTodoId(id);
            List<TodoAttachmentsDto> todoAttachmentsDtoList = new ArrayList<>();

            for (TodoAttachments todoAttachment : images) {
                TodoAttachmentsDto todoAttachmentsDto = new TodoAttachmentsDto();
                todoAttachmentsDto.setId(todoAttachment.getId());
                todoAttachmentsDto.setTodoId(todoAttachment.getTodo().getId());
                todoAttachmentsDto.setType(todoAttachment.getType());
                todoAttachmentsDto.setFileName(todoAttachment.getFileName());
                todoAttachmentsDto.setImageName(todoAttachment.getImageName());
                todoAttachmentsDto.setPath(todoAttachment.getPath());
                todoAttachmentsDto.setLink(todoAttachment.getLink());
                todoAttachmentsDto.setLinkName(todoAttachment.getLinkName());
                todoAttachmentsDtoList.add(todoAttachmentsDto);
            }
            todoDto.setTodoAttachmentsDtos(todoAttachmentsDtoList);

            List<TodoAssign> todoAssignDtoList = this.todoAssignRepository.getByTodoId(id);
            List<Map<String, Object>> todoAssignData = new ArrayList<>();
            if (todoAssignDtoList != null) {
                for (TodoAssign todoAssign : todoAssignDtoList) {
                    Map<String, Object> todoAssignDataMap = new HashMap<>();
                    todoAssignDataMap.put("id", todoAssign.getId());
                    todoDto.setTeamName(todoAssign.getTeamDetails() != null ? todoAssign.getTeamDetails().getName() : null);
                    todoAssignDataMap.put("todoId", todoAssign.getTodo().getId());
                    todoAssignDataMap.put("userId", todoAssign.getCustomers().getId());
                    todoAssignDataMap.put(
                            "userName",
                            (todoAssign.getCustomers().getUsername() != null
                                    && !todoAssign.getCustomers().getUsername().trim().isEmpty())
                                    ? todoAssign.getCustomers().getUsername()
                                    : todoAssign.getAssignBy().getFirstName() + " " + todoAssign.getAssignBy().getLastName()
                    );

                    todoAssignDataMap.put("assignedById", todoAssign.getAssignBy().getId());
                    todoAssignDataMap.put(
                            "assignedByName",
                            (todoAssign.getAssignBy().getUsername() != null
                                    && !todoAssign.getAssignBy().getUsername().trim().isEmpty())
                                    ? todoAssign.getAssignBy().getUsername()
                                    : todoAssign.getAssignBy().getFirstName() + " " + todoAssign.getAssignBy().getLastName()
                    );
                    todoAssignDataMap.put("complectedWork", todoAssign.getComplectedWork());
                    todoAssignData.add(todoAssignDataMap);
                }
            }
//            List<TodoAttachments> images2 = this.todoAttachmentsRepository.getByTodoId(id);
//            List<ImageResponseDto> imageResponseDtos = new ArrayList<>();
//
//            for (TodoAttachments todoAttachment : images2) {
//                ImageResponseDto imageResponseDto = new ImageResponseDto();
//                imageResponseDto.setImageId(todoAttachment.getId());
//                imageResponseDto.setImageURL(todoAttachment.getPath());
//                imageResponseDtos.add(imageResponseDto);
//            }
//            todoDto.setImages(imageResponseDtos);

            todoDto.setTodoAssignData(todoAssignData);
            return todoDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoDto createTodo(TodoDto todoDto, Boolean syncToSalesforce) {
        try {
            Todo todo = new Todo();
            if (todoDto.getDueDate() != null) {
                todo.setDueDate(this.commonService.convertStringToDate(todoDto.getDueDate()));
            }
            Customers createdBy = this.customersRepository.findById(todoDto.getCreatedBy()).orElseThrow(() -> new RuntimeException("Customer not found"));
            todo.setCreatedBy(createdBy);

            todo.setIsDeleted(false);
            BeanUtils.copyProperties(todoDto, todo, "id", "isDeleted", "createdBy", "customers", "isToday");
            Todo savedTodo = this.todoRepository.save(todo);

            if (!todoDto.getTodoAttachmentsDtos().isEmpty()) {
                for (TodoAttachmentsDto attachmentsDto : todoDto.getTodoAttachmentsDtos()) {
                    TodoAttachments todoAttachments = new TodoAttachments();
                    todoAttachments.setType(attachmentsDto.getType());
                    if (attachmentsDto.getType().equals("File")) {
                        todoAttachments.setTodo(savedTodo);
                        todoAttachments.setFileName(attachmentsDto.getFileName());
                        todoAttachments.setImageName(attachmentsDto.getImageName());
                        todoAttachments.setPath(attachmentsDto.getPath());
                        this.todoAttachmentsRepository.save(todoAttachments);

                        String updatedPath = this.commonService.updateFileLocation(attachmentsDto.getPath(), todoDto.getCreatedBy(), "todo/", "todo/" + todo.getId() + "/" + todoAttachments.getId());
                        todoAttachments.setPath(updatedPath);
                        this.todoAttachmentsRepository.save(todoAttachments);

                    } else {
                        todoAttachments.setTodo(savedTodo);
                        todoAttachments.setLink(attachmentsDto.getLink());
                        todoAttachments.setLinkName(attachmentsDto.getLinkName());
                        this.todoAttachmentsRepository.save(todoAttachments);
                    }
                }
            }
            TodoDto savedTodoDto = new TodoDto();
            BeanUtils.copyProperties(savedTodo, savedTodoDto);
            return savedTodoDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoDto updateTodo(Integer id, TodoDto todoDto, Boolean syncToSalesforce) {
        try {
            Todo todo = this.todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found"));

            if (todoDto.getDueDate() != null) {
                todo.setDueDate(this.commonService.convertStringToDate(todoDto.getDueDate()));
            }
            todo.setIsDeleted(false);
            BeanUtils.copyProperties(todoDto, todo, "id", "isDeleted", "createdBy", "customers", "isToday");
            this.todoRepository.save(todo);

            if (todoDto.getTodoAttachmentsDtos() != null && !todoDto.getTodoAttachmentsDtos().isEmpty()) {
                for (TodoAttachmentsDto attachmentsDto : todoDto.getTodoAttachmentsDtos()) {

                    TodoAttachments todoAttachments;

                    // ✅ UPDATE if id present, otherwise CREATE
                    if (attachmentsDto.getId() != null) {
                        todoAttachments = this.todoAttachmentsRepository
                                .findById(attachmentsDto.getId())
                                .orElseThrow(() -> new RuntimeException("Attachment not found: " + attachmentsDto.getId()));

                        // Safety check: ensure it belongs to this todo
                        if (todoAttachments.getTodo() == null || !todoAttachments.getTodo().getId().equals(todo.getId())) {
                            throw new RuntimeException("Attachment does not belong to this Todo");
                        }
                    } else {
                        todoAttachments = new TodoAttachments();
                        todoAttachments.setTodo(todo);
                    }

                    todoAttachments.setType(attachmentsDto.getType());

                    if ("File".equals(attachmentsDto.getType())) {

                        // ✅ Always allow renaming
                        todoAttachments.setFileName(attachmentsDto.getFileName());

                        // ✅ Only update/move file if a NEW file was uploaded (path/imageName changed)
                        boolean fileChanged =
                                (attachmentsDto.getImageName() != null && !attachmentsDto.getImageName().isBlank()
                                        && !attachmentsDto.getImageName().equals(todoAttachments.getImageName()))
                                        || (attachmentsDto.getPath() != null && !attachmentsDto.getPath().isBlank()
                                        && !attachmentsDto.getPath().equals(todoAttachments.getPath()));

                        if (fileChanged) {
                            todoAttachments.setImageName(attachmentsDto.getImageName());
                            todoAttachments.setPath(attachmentsDto.getPath());
                            this.todoAttachmentsRepository.save(todoAttachments);

                            String updatedPath = this.commonService.updateFileLocation(
                                    attachmentsDto.getPath(),
                                    todoDto.getCreatedBy(),
                                    "todo/",
                                    "todo/" + todo.getId() + "/" + todoAttachments.getId()
                            );

                            todoAttachments.setPath(updatedPath);
                        }

                        this.todoAttachmentsRepository.save(todoAttachments);

                    } else { // Link
                        todoAttachments.setLinkName(attachmentsDto.getLinkName());
                        todoAttachments.setLink(attachmentsDto.getLink());
                        this.todoAttachmentsRepository.save(todoAttachments);
                    }
                }
            }
            return todoDto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteTodo(Integer id, Boolean syncToSalesforce) {
        try {
            Todo todo = this.todoRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo not found"));
            List<TodoAttachments> todoAttachmentsList = this.todoAttachmentsRepository.getByTodoId(id);
            if (!todoAttachmentsList.isEmpty()) {
                for (TodoAttachments todoAttachments : todoAttachmentsList) {
                    this.todoAttachmentsService.deleteAttachment(todoAttachments.getId());
                }
            }
            this.todoRepository.delete(todo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteImagesById(Integer imageId) {
        try {
            TodoAttachments images = this.todoAttachmentsRepository.findById(imageId).orElseThrow(() -> new RuntimeException("Image not found"));
            String path = images.getPath().split("usercontent/")[1];

            // Remove the file name part after the last slash (116_download_20241126_131647.png)
            path = path.substring(0, path.lastIndexOf("/"));

            // Add leading slash to the final result
            path = "/" + path;
            File existingImagePath = new File(FILE_DIRECTORY + path);
            if (existingImagePath.exists()) {
                this.commonService.deleteDirectoryRecursively(existingImagePath);
                this.todoAttachmentsRepository.delete(images);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void completeTodo(Integer todoId) {
        try {
            Todo todo = this.todoRepository.findById(todoId).orElseThrow(() -> new RuntimeException("Todo not found"));
            List<TodoAssign> todoAssignDtoList = this.todoAssignRepository.getByTodoId(todoId);
            if (!todoAssignDtoList.isEmpty()) {
                for (TodoAssign todoAssign : todoAssignDtoList) {
                    this.todoAssignService.setStatusToCompleted(todoAssign.getId());
                }
            }
            todo.setComplectedWork(100);
            this.todoRepository.save(todo);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}