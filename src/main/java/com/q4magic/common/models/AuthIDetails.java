package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "auth_id_details")
@Setter
@Getter
@NoArgsConstructor
public class AuthIDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id  ", unique = true, nullable = false)
    private Integer id;

    @Column(name = "document_type")
    private Integer documentType;

    @Column(name = "email")
    private String email;

    @Column(name = "auth_account_number")
    private String authAccountNumber;

    @Column(name = "auth_operation_id")
    private String authOperationId;

    @Column(name = "auth_selfie_operation_id")
    private String authSelfieOperationId;

    @Column(name = "registered_date")
    @Temporal(TemporalType.DATE)
    private Date registeredDate;
}
