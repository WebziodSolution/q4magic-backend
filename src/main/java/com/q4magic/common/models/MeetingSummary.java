package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "meeting_summary")
@Setter
@Getter
@NoArgsConstructor
public class MeetingSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "transcript", columnDefinition = "LONGTEXT")
    private String transcript;

    @Column(name = "summary", columnDefinition = "LONGTEXT")
    private String summary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name = "created_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
}
