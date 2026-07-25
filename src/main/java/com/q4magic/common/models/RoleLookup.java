package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_lookup")
@Setter
@Getter
@NoArgsConstructor
public class RoleLookup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "role_type")
    private String roleType;

    @Column(name = "role")
    private String role;
}
