package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name="calendar_details")
public class CalendarDetails {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id", unique=true, nullable=false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cald_cal_id", referencedColumnName = "id")
    private Calendar calendar;

    @Column(name="cald_type")
    private String caldType;

    @Column(name="cald_syc_id")
    private String caldSycId;
}
