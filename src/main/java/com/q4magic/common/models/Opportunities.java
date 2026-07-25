package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "opportunities")
@Setter
@Getter
@NoArgsConstructor
public class Opportunities {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opp_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "salesforce_opportunity_id")
    private String salesforceOpportunityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id", referencedColumnName = "acc_id")
    private Account account;

    @Column(name = "opportunity")
    private String opportunity;

    @Column(name = "sales_stage")
    private String salesStage;

    @Column(name = "deal_amount")
    private Integer dealAmount;

    @Column(name = "discount_percentage")
    private Integer discountPercentage;

    @Column(name = "list_price")
    private Integer listPrice;

    @Column(name = "close_date")
    @Temporal(TemporalType.DATE)
    private Date closeDate;

    @Column(name = "next_steps")
    private String nextSteps;

    @Column(name = "why_do_anything", columnDefinition = "longtext")
    private String whyDoAnything;

    @Column(name = "business_value", columnDefinition = "longtext")
    private String businessValue;

    @Column(name = "current_environment", columnDefinition = "longtext")
    private String currentEnvironment;

    @Column(name = "decision_map", columnDefinition = "longtext")
    private String decisionMap;

    @Column(name = "status")
    private String status;

    @Column(name = "forecast_date")
    @Temporal(TemporalType.DATE)
    private Date forecastDate;

    @Column(name = "decision_criteria")
    private String decisionCriteria;

    @Column(name = "record_status")
    private String recordStatus;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "partners")
    private String partners;

    @Column(name = "logo")
    private String logo;

    @Column(name = "domain")
    private String domain;
}
