package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "sales_process")
@Setter
@Getter
@NoArgsConstructor
public class SalesProcess {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_pro_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @Column(name = "go_live")
    @Temporal(TemporalType.DATE)
    private Date goLive;

    @Column(name = "process_date")
    @Temporal(TemporalType.DATE)
    private Date processDate;

    @Column(name = "process")
    private String process;

    @Column(name = "notes")
    private String notes;

    @Column(name = "reason")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", referencedColumnName = "id")
    private OpportunityContact contacts;
}
