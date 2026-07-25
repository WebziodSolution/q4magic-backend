package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "contacts")
@Setter
@Getter
@NoArgsConstructor
public class Contacts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "con_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "salesforce_contact_id")
    private String salesforceContactId;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "phone")
    private String phone;

    @Column(name = "linkedin_profile")
    private String linkedinProfile;

    @Column(name = "title")
    private String title;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "role")
    private String role;

    @Column(name = "notes")
    private String notes;

    @Column(name = "record_status")
    private String recordStatus;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "cus_id")
    private Customers customers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_to", referencedColumnName = "con_id")
    private Contacts contacts;

    @Column(name = "salesforce_account_id")
    private String salesforceAccountId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "acc_id")
    private Account account;

    @Column(name = "created_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Column(name = "from_mail_scraping")
    private Boolean fromMailScraping;
}
