package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "todo_assignees")
@Setter
@Getter
@NoArgsConstructor
public class TodoAssign {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_ass_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", referencedColumnName = "todo_id")
    private Todo todo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id_assignee", referencedColumnName = "cus_id")
    private Customers customers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", referencedColumnName = "id")
    private TeamDetails teamDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_by", referencedColumnName = "cus_id")
    private Customers assignBy;

    @Column(name = "complected_work")
    private Integer complectedWork;

    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Column(name = "priority")
    private String priority;
}
