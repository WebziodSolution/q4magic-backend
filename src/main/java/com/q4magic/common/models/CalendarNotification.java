package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name="calendar_notification")
public class CalendarNotification {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id", unique=true, nullable=false)
    private Integer id;

    @Column(name="minutes")
    private Long minutes;

    @Column(name="notification")
    private String notification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calender_id", referencedColumnName = "id")
    private Calendar calendar;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    @Column(name = "send_date_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date sendDateTime;
}
