package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name="calendar_appointment_availability_slots")
public class CalendarAppointmentAvailabilitySlots {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id", unique=true, nullable=false)
    private Integer id;

    @Column(name="day_name")
    private String dayName;

    @Column(name="start_time")
    private Time startTime;

    @Column(name="end_time")
    private Time endTime;

    @Builder.Default
    @Column(name="available", columnDefinition = "String default Y")
    private String available = String.valueOf('Y');

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created_date")
    private Date createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "cus_id")
    private Customers customers;
}
