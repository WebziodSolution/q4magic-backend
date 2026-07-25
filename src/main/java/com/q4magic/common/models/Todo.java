package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "todo")
@Setter
@Getter
@NoArgsConstructor
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "related_to")
    private String relatedTo;

    @Column(name = "task")
    private String task;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date")
    @Temporal(TemporalType.DATE)
    private Date dueDate;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "cus_id")
    private Customers createdBy;

    @Column(name = "complected_work")
    private Integer complectedWork;

    @Column(name = "priority")
    private String priority;
}