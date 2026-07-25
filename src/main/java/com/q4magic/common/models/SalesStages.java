package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sales_stages")
@Setter
@Getter
@NoArgsConstructor
public class SalesStages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sales_stage_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "salesforce_stage_id")
    private String salesforceStageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crm_crm_id", referencedColumnName = "crm_id")
    private CRM crm;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "description")
    private String description;
}
