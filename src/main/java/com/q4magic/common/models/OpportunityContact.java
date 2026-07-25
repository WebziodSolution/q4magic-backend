package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opportunity_contact")
@Setter
@Getter
@NoArgsConstructor
public class OpportunityContact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "salesforce_opportunity_contact_id")
    private String salesforceOpportunityContactId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", referencedColumnName = "con_id")
    private Contacts contacts;

    @Column(name = "salesforce_contact_id")
    private String salesforceContactId;

    @Column(name = "role")
    private String role;

    @Column(name = "title")
    private String title;

    @Column(name = "is_key")
    private Boolean isKey;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
