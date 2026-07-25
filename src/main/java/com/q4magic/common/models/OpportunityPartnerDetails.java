package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opportunities_partner_details")
@Setter
@Getter
@NoArgsConstructor
public class OpportunityPartnerDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "account_to_Id")
    private String accountToId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_Id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @Column(name = "opportunity_partner_id")
    private String salesforceOpportunityPartnerId;

    @Column(name = "account_Id")
    private String accountId;

    @Column(name = "role")
    private String role;

    @Column(name = "is_primary")
    private Boolean isPrimary;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
