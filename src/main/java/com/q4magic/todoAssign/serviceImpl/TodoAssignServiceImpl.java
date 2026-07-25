package com.q4magic.todoAssign.serviceImpl;

import com.q4magic.common.dto.TodoAssignDto;
import com.q4magic.common.models.*;
import com.q4magic.common.repository.*;
import com.q4magic.common.service.CommonService;
import com.q4magic.todoAssign.service.TodoAssignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service(value = "TodoAssignService")
public class TodoAssignServiceImpl implements TodoAssignService {
    @Value("${siteUrl}")
    String siteUrl;
    
    @Autowired
    private TodoAssignRepository todoAssignRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private CustomersRepository customersRepository;

    @Autowired
    private TeamDetailsRepository teamDetailsRepository;

    @Autowired
    private TeamMembersRepository teamMembersRepository;

    @Autowired
    private CommonService commonService;

    @Override
    public List<TodoAssignDto> getAllTodosAssignToMe(Integer id) {
        try {
            List<TodoAssign> todoAssignList = this.todoAssignRepository.findAll();
            List<TodoAssignDto> todoAssignDtoList = new ArrayList<>();
            if (!todoAssignList.isEmpty()) {
                for (TodoAssign todoAssign : todoAssignList) {
                    if (todoAssign.getCustomers() != null) {
                        if (todoAssign.getCustomers().getId().equals(id)) {
                            todoAssignDtoList.add(this.getAssignTodoById(todoAssign.getId(), null));
                        }
                    }
                }
            }
            return todoAssignDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoAssignDto getAllByTodoId(Integer todoId, Integer loginUserId) {
        try {
            List<TodoAssign> todoAssignList = this.todoAssignRepository.getByTodoId(todoId);
            if (todoAssignList == null || todoAssignList.isEmpty()) {
                throw new RuntimeException("Todo Assign not found");
            }

            // ✅ Safely group by teamId (null-safe)
            Map<Integer, List<TodoAssign>> groupedByTeam = todoAssignList.stream()
                    .collect(Collectors.groupingBy(a ->
                            a.getTeamDetails() != null ? a.getTeamDetails().getId() : 0
                    ));


            // ✅ Iterate groups
            for (Map.Entry<Integer, List<TodoAssign>> entry : groupedByTeam.entrySet()) {
                List<TodoAssign> assignsForTeam = entry.getValue();

                // Only consider groups with non-null team and multiple records
                if (assignsForTeam.size() >= 1) {
                    List<Integer> customerIds = assignsForTeam.stream()
                            .map(a -> a.getCustomers().getId())
                            .collect(Collectors.toList());

                    TodoAssignDto dto = this.getAssignTodoById(todoId, customerIds.get(0));
                    dto.setCustomerIds(customerIds);
                    return dto;
                }
            }

            // ✅ No duplicate teams found — return first record
            return this.getAssignTodoById(todoId, loginUserId);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<TodoAssignDto> getAllAssignedTodos(Integer createdBy) {
        try {
            List<TodoAssign> todoAssignList = this.todoAssignRepository.getAllAssignBy(createdBy);
            List<TodoAssignDto> todoAssignDtoList = new ArrayList<>();
            if (!todoAssignList.isEmpty()) {
                for (TodoAssign todoAssign : todoAssignList) {
                    todoAssignDtoList.add(this.getAssignTodoById(todoAssign.getId(), null));
                }
            }
            return todoAssignDtoList;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public TodoAssignDto getAssignTodoById(Integer id, Integer userId) {
        try {
            TodoAssign todoAssign = new TodoAssign();
            if (userId != null) {
                todoAssign = this.todoAssignRepository.getByCustomerAndTodo(userId, id);
            } else {
                todoAssign = this.todoAssignRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Todo Assign not found"));
            }
            if (todoAssign == null) {
                return null;
            }

            TodoAssignDto dto = new TodoAssignDto();
            dto.setId(todoAssign.getId());

            if (todoAssign.getCustomers() != null) {
                dto.setCustomerId(todoAssign.getCustomers().getId());
            }

            if (todoAssign.getTeamDetails() != null) {
                dto.setTeamId(todoAssign.getTeamDetails().getId());
            }

            if (todoAssign.getTodo() != null) {
                dto.setTodoId(todoAssign.getTodo().getId());
            }

            if (todoAssign.getAssignBy() != null) {
                dto.setAssignBy(todoAssign.getAssignBy().getId());
            }

            dto.setComplectedWork(todoAssign.getComplectedWork());
            dto.setPriority(todoAssign.getPriority());

            if (todoAssign.getDueDate() != null) {
                dto.setDueDate(this.commonService.convertDateToString(todoAssign.getDueDate()));
            }

            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error retrieving assigned todo", e);
        }
    }

    @Override
    public TodoAssignDto assignTodoToUser(TodoAssignDto todoAssignDto) {
        try {
            // 1. Process removals – delete assignments for customers in removeCustomerIds
            if (todoAssignDto.getRemoveCustomerIds() != null && !todoAssignDto.getRemoveCustomerIds().isEmpty()) {
                for (Integer removeCusId : todoAssignDto.getRemoveCustomerIds()) {
                    TodoAssign existing = todoAssignRepository.getByCustomerAndTodo(removeCusId, todoAssignDto.getTodoId());
                    if (existing != null) {
                        todoAssignRepository.delete(existing);
                    }
                }
            }

            // 2. Determine the set of customer IDs that should be assigned after this operation
            Set<Integer> targetCustomerIds = new HashSet<>();
            Integer teamId = todoAssignDto.getTeamId(); // may be null

            if (todoAssignDto.getCustomerId() != null && teamId == null) {
                // Single customer assignment (no team)
                targetCustomerIds.add(todoAssignDto.getCustomerId());
            } else if (todoAssignDto.getCustomerIds() != null && !todoAssignDto.getCustomerIds().isEmpty()) {
                // Bulk customer assignment (possibly with a team)
                targetCustomerIds.addAll(todoAssignDto.getCustomerIds());
            } else if (teamId != null) {
                // Team assignment – get all team members
                List<TeamMembers> teamMembers = teamMembersRepository.findByTeamId(teamId);
                if (teamMembers.isEmpty()) {
                    throw new RuntimeException("No team members found for the given team");
                }
                for (TeamMembers member : teamMembers) {
                    targetCustomerIds.add(member.getId()); // assuming member.getId() is the customer ID
                }
            } else {
                // No target specified – nothing to do
                return todoAssignDto;
            }

            // 3. Process each target customer: create or update assignment, send email only for new ones
            for (Integer customerId : targetCustomerIds) {
                // Find any existing assignment for this customer and todo (team is not part of the identity)
                TodoAssign todoAssign = todoAssignRepository.getByCustomerAndTodo(customerId, todoAssignDto.getTodoId());
                boolean isNew = false;
                if (todoAssign == null) {
                    todoAssign = new TodoAssign();
                    isNew = true;
                }

                // Set common fields
                Customers customer = customersRepository.findById(customerId)
                        .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
                todoAssign.setCustomers(customer);

                Todo todo = todoRepository.findById(todoAssignDto.getTodoId())
                        .orElseThrow(() -> new RuntimeException("Todo not found"));
                todoAssign.setTodo(todo);

                Customers assignBy = customersRepository.findById(todoAssignDto.getAssignBy())
                        .orElseThrow(() -> new RuntimeException("Assign By Customer not found"));
                todoAssign.setAssignBy(assignBy);

                // Set optional fields (uncomment and adjust as needed)
                if (todoAssignDto.getComplectedWork() != null) {
                    todoAssign.setComplectedWork(todoAssignDto.getComplectedWork());
                }
                if (todoAssignDto.getDueDate() != null) {
                    todoAssign.setDueDate(commonService.convertStringToDate(todoAssignDto.getDueDate()));
                }
                if (todoAssignDto.getPriority() != null) {
                    todoAssign.setPriority(todoAssignDto.getPriority());
                }

                // Set team if provided (otherwise leave as null)
                if (teamId != null) {
                    TeamDetails teamDetails = teamDetailsRepository.findById(teamId)
                            .orElseThrow(() -> new RuntimeException("Team not found"));
                    todoAssign.setTeamDetails(teamDetails);
                } else {
                    todoAssign.setTeamDetails(null); // ensure no stale team remains
                }

                // Save the assignment
                this.todoAssignRepository.save(todoAssign);

                // 4. Send email only if this is a brand new assignment
                if (isNew) {
                    ZoneId zoneId = ZoneId.systemDefault();
                    String formattedDueDate = commonService.formatDueLongZoned(
                            commonService.convertDateToString(todoAssign.getDueDate()),
                            zoneId
                    );
                    sendTaskAssignedEmail(
                            todo.getRelatedTo(),
                            customer.getFirstName() + " " + customer.getLastName(),
                            customer.getEmailAddress(),
                            todo.getTask(),
                            formattedDueDate
                    );
                }
            }

            return todoAssignDto;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAssignTodo(Integer id) {
        try {
            TodoAssign todoAssign = this.todoAssignRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo Assign not found"));
            this.todoAssignRepository.delete(todoAssign);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setStatusToCompleted(Integer id) {
        try {
            TodoAssign todoAssign = this.todoAssignRepository.findById(id).orElseThrow(() -> new RuntimeException("Todo Assign not found"));
            todoAssign.setComplectedWork(100);
            this.todoAssignRepository.save(todoAssign);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendTaskReminder(Integer userId, Integer todoId, Integer assignId) {
        try {
            Customers customers = this.customersRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));

            Todo todo = this.todoRepository.findById(todoId)
                    .orElseThrow(() -> new RuntimeException("Task not found"));

            TodoAssign todoAssign = this.todoAssignRepository.findById(assignId)
                    .orElseThrow(() -> new RuntimeException("Todo Assign not found"));

            if (todoAssign.getDueDate() == null) {
                return; // No due date, cannot send reminder
            }

            ZoneId zoneId = ZoneId.systemDefault();

            // SAFE CONVERSION: java.sql.Date → java.util.Date → LocalDate
            java.util.Date safeDate = new java.util.Date(todoAssign.getDueDate().getTime());
            LocalDate dueDate = safeDate.toInstant().atZone(zoneId).toLocalDate();

            LocalDate today = LocalDate.now(zoneId);

            long daysUntilDue = ChronoUnit.DAYS.between(today, dueDate);

            String formattedDueDate = this.commonService.formatDueLongZoned(
                    this.commonService.convertDateToString(todoAssign.getDueDate()),
                    zoneId
            );

            // Common parameters
            String recipientName = customers.getUsername();
            String recipientEmail = customers.getEmailAddress();
            String taskTitle = todo.getTask();

            // Decide which reminder to send based on daysUntilDue
            if (daysUntilDue == 2) {
                // Due Soon (2 days before)
                this.sendDueSoonReminderEmail(
                        todo.getRelatedTo(),
                        recipientName,
                        recipientEmail,
                        taskTitle,
                        formattedDueDate
                );
            } else if (daysUntilDue == 0) {
                // Due Today
                this.sendDueTodayReminderEmail(
                        todo.getRelatedTo(),
                        recipientName,
                        recipientEmail,
                        taskTitle
                );
            } else if (daysUntilDue == -1) {
                // Past Due (1 day after missed)
                this.sendPastDueReminderEmail(
                        todo.getRelatedTo(),
                        recipientName,
                        recipientEmail,
                        taskTitle,
                        formattedDueDate
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean sendTaskAssignedEmail(String relatedTo, String recipientName, String recipientEmail, String taskTitle, String dueDate) {
        try {
            // Subject as per new template
            String subject = "New Priority Assigned – " + taskTitle;

            // Get current year for footer
            int currentYear = java.time.Year.now().getValue();

            // HTML email body with header, footer, and original content
            String body = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<title>New Priority Assigned</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                    ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                    ".header { text-align: center; padding: 20px 0; border-bottom: 2px solid #eee; }" +
                    ".logo { max-width: 180px; height: auto; }" +
                    ".content { padding: 20px 0; }" +
                    ".button {" +
                    "  display: inline-block;" +
                    "  padding: 12px 24px;" +
                    "  background-color: #007bff;" +
                    "  color: #ffffff !important;" +
                    "  text-decoration: none;" +
                    "  border-radius: 4px;" +
                    "  font-weight: bold;" +
                    "}" +
                    ".button:hover { background-color: #0056b3; }" +
                    ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee; font-size: 0.9em; color: #777; text-align: center; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class=\"container\">" +
                    "<div class=\"header\">" +
                    "<img src=\""+siteUrl+"images/logo/360Pipe_logo.png\" alt=\"360Pipe Logo\" class=\"logo\">" +
                    "</div>" +
                    "<div class=\"content\">" +
                    "<p>Hi " + recipientName + ",</p>" +
                    "<p>A new priority has been assigned to you:</p>" +
                    "<ul>" +
                    "  <li><strong>Related To:</strong> " + relatedTo + "</li>" +
                    "  <li><strong>Action:</strong> " + taskTitle + "</li>" +
                    "  <li><strong>Due Date:</strong> " + (dueDate != null ? dueDate : "Not specified") + "</li>" +
                    "</ul>" +
                    "<p>Please review and take action inside <a href=\""+siteUrl+"/dashboard/todos\">360Pipe</a>.</p>" +
                    "<p>Visibility drives progression — keep your deal momentum moving.</p>" +
                    "</div>" +
                    "<div class=\"footer\">" +
                    "<p>&copy; " + currentYear + " 360Pipe. All rights reserved.</p>" +
                    "</div>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            // Send as HTML
            return this.commonService.sendEmail(recipientEmail, subject, body, true);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending Priority Assigned email", e);
        }
    }

    private boolean sendDueSoonReminderEmail(
            String relatedTo,
            String recipientName,
            String recipientEmail,
            String taskTitle,
            String dueDate
    ) {
        try {
            // Subject as per new template
            String subject = "Priority Due Soon – " + taskTitle;

            // Format due date
            String dueDateText = (dueDate != null && !dueDate.isBlank()) ? dueDate : "Not specified";

            // HTML email body matching the new template
            String body = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<title>Priority Due Soon</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                    ".button {" +
                    "  display: inline-block;" +
                    "  padding: 12px 24px;" +
                    "  background-color: #007bff;" +
                    "  color: #ffffff !important;" +
                    "  text-decoration: none;" +
                    "  border-radius: 4px;" +
                    "  font-weight: bold;" +
                    "}" +
                    ".button:hover { background-color: #0056b3; }" +
                    ".footer { margin-top: 30px; font-size: 0.9em; color: #777; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<p>Hi " + (recipientName != null ? recipientName : "User") + ",</p>" +
                    "<p>Reminder: The following priority is due soon:</p>" +
                    "<ul>" +
                    "  <li><strong>Related To:</strong> " + (relatedTo != null ? relatedTo : "") + "</li>" +
                    "  <li><strong>Action:</strong> " + (taskTitle != null ? taskTitle : "") + "</li>" +  // Using taskTitle for Action; adjust if separate field exists
                    "  <li><strong>Due Date:</strong> " + dueDateText + "</li>" +
                    "</ul>" +
                    "<p>Please ensure next steps are scheduled and progressing.</p>" +
                    "<p class=\"footer\">— 360Pipe</p>" +
                    "</body>" +
                    "</html>";

            // Send as HTML
            return this.commonService.sendEmail(recipientEmail, subject, body, true);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending Due Soon Reminder email", e);
        }
    }


    private boolean sendDueTodayReminderEmail(
            String relatedTo,
            String recipientName,
            String recipientEmail,
            String taskTitle
    ) {
        try {
            // Subject as per template
            String subject = "Priority Due Today – " + taskTitle;

            // HTML email body matching the new template
            String body = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<title>Priority Due Today</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                    ".button {" +
                    "  display: inline-block;" +
                    "  padding: 12px 24px;" +
                    "  background-color: #007bff;" +
                    "  color: #ffffff !important;" +
                    "  text-decoration: none;" +
                    "  border-radius: 4px;" +
                    "  font-weight: bold;" +
                    "}" +
                    ".button:hover { background-color: #0056b3; }" +
                    ".footer { margin-top: 30px; font-size: 0.9em; color: #777; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<p>Hi " + (recipientName != null ? recipientName : "User") + ",</p>" +
                    "<p>The following priority is due today:</p>" +
                    "<ul>" +
                    "  <li><strong>Related To:</strong> " + (relatedTo != null ? relatedTo : "") + "</li>" +
                    "  <li><strong>Action:</strong> " + (taskTitle != null ? taskTitle : "") + "</li>" +  // Use separate field if available
                    "</ul>" +
                    "<p>Please complete or update the status in 360Pipe.</p>" +
                    "<p class=\"footer\">— 360Pipe</p>" +
                    "</body>" +
                    "</html>";

            // Send as HTML
            return this.commonService.sendEmail(recipientEmail, subject, body, true);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending Due Today Reminder email", e);
        }
    }


    private boolean sendPastDueReminderEmail(
            String relatedTo,
            String recipientName,
            String recipientEmail,
            String taskTitle,
            String dueDate
    ) {
        try {
            // Subject as per template
            String subject = "Priority Past Due – " + taskTitle;

            // Format due date
            String dueDateText = (dueDate != null && !dueDate.isBlank()) ? dueDate : "Not specified";

            // HTML email body matching the new template
            String body = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset=\"UTF-8\">" +
                    "<title>Priority Past Due</title>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                    ".button {" +
                    "  display: inline-block;" +
                    "  padding: 12px 24px;" +
                    "  background-color: #007bff;" +
                    "  color: #ffffff !important;" +
                    "  text-decoration: none;" +
                    "  border-radius: 4px;" +
                    "  font-weight: bold;" +
                    "}" +
                    ".button:hover { background-color: #0056b3; }" +
                    ".footer { margin-top: 30px; font-size: 0.9em; color: #777; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<p>Hi " + (recipientName != null ? recipientName : "User") + ",</p>" +
                    "<p>The following priority is now past due:</p>" +
                    "<ul>" +
                    "  <li><strong>Related To:</strong> " + (relatedTo != null ? relatedTo : "") + "</li>" +
                    "  <li><strong>Action:</strong> " + (taskTitle != null ? taskTitle : "") + "</li>" +  // Use separate field if available
                    "  <li><strong>Original Due Date:</strong> " + dueDateText + "</li>" +
                    "</ul>" +
                    "<p>Please review and update the status in 360Pipe.</p>" +
                    "<p>Past-due priorities are visible in your team dashboard.</p>" +
                    "<p class=\"footer\">— 360Pipe</p>" +
                    "</body>" +
                    "</html>";

            // Send as HTML
            return this.commonService.sendEmail(recipientEmail, subject, body, true);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error sending Past Due Reminder email", e);
        }
    }
}