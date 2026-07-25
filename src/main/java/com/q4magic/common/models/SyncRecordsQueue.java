package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "sync_records_queue")
@Setter
@Getter
@NoArgsConstructor
public class SyncRecordsQueue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id ", unique = true, nullable = false)
    private Integer id;

    @Column(name = "subject_id")
    private Integer subjectId;

    @Column(name = "subject")
    private String subject;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "sync_type")
    private String syncType;

    @Column(name = "error")
    private String error;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name = "is_deleted", columnDefinition = "boolean default false")
    private boolean isDeleted;
}
