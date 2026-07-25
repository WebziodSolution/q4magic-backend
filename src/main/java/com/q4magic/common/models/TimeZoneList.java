package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name="time_zones")
public class TimeZoneList {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "id", unique=true, nullable=false)
    private Integer tmzId;

    @Column(name="tmz_title", nullable=false, length=255)
    private String tmzTitle;

    @Column(name="tmz_value", nullable=false, length=255)
    private String tmzValue;
}
