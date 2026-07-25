package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "close_plan_notes")
public class ClosePlanNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "clo_plan_note_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clo_id", referencedColumnName = "clo_id")
    private ClosePlan closePlan;

    @Column(name = "send_to")
    private Integer sendTo;

    @Column(name = "comments")
    private String comments;

    @Column(name = "created_by")
    private Integer createdBy;

    @Column(name = "created_at",columnDefinition = "timestamp DEFAULT CURRENT_TIMESTAMP")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();
}
