package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "module_actions")
@Setter
@Getter
@NoArgsConstructor
public class ModuleActions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", referencedColumnName = "id", columnDefinition = "number")
    private Modules module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", referencedColumnName = "id", columnDefinition = "number")
    private Actions action;

    @OneToMany(mappedBy = "moduleActions", fetch = FetchType.LAZY)
    private Set<RoleModuleActions> moduleActions;
}
