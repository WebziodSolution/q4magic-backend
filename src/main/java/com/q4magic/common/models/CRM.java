package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "crm")
@Setter
@Getter
@NoArgsConstructor
public class CRM {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crm_id", unique = true, nullable = false)
    private Integer crmId;

    @Column(name = "name")
    private String name;
}
