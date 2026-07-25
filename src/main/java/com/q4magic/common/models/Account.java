package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@Setter
@Getter
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acc_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crm_id", referencedColumnName = "crm_id")
    private CRM crm;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "link")
    private String link;

    @Column(name = "record_status")
    private String recordStatus;

    @Column(name = "logo")
    private String logo;

    @Column(name = "salesforce_account_id")
    private String salesforceAccountId;

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "cus_id")
    private Customers customers;
}