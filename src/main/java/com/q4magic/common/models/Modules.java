package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "modules")
@Setter
@Getter
@NoArgsConstructor
public class Modules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "functionality_id", referencedColumnName = "id")
    private Functionality functionality;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY)
    private Set<ModuleActions> moduleActions;
}
