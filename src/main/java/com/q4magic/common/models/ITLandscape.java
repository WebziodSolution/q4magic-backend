package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "it_landscape")
@Setter
@Getter
@NoArgsConstructor
public class ITLandscape {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "it_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "salesforce_competitor_id")
    private String salesforceCompetitorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
    private Customers customers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @Column(name = "salesforce_opportunity_id")
    private String salesforceOpportunityId;

    @Column(name = "it_category")
    private String itCategory;

    @Column(name = "sub_category")
    private String subCategory;

    @Column(name = "vendor")
    private String vendor;

    @Column(name = "competitor_flag")
    private String competitorFlag;

    @Column(name = "partner_flag")
    private String partnerFlag;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
