package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "opportunity_comments")
@Setter
@Getter
@NoArgsConstructor
public class OpportunityComments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opp_com_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name = "title")
    private String title;

    @Column(name = "comment")
    private String comment;

    @Column(name = "comment_date")
    @Temporal(TemporalType.DATE)
    private Date commentDate;
}
