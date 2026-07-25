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
@Table(name="close_plan")
public class ClosePlan {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name = "clo_id", unique=true, nullable=false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
    private Customers customers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", referencedColumnName = "con_id")
    private Contacts contacts;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "status")
    private String status;

    @Column(name = "status_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date statusTime;

    @Column(name = "url")
    private String url;
}
