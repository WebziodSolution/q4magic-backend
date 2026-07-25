package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_module_actions")
@Setter
@Getter
@NoArgsConstructor
public class RoleModuleActions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer roleActionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", columnDefinition = "NUMBER")
    private SubUserType role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_action_Id", referencedColumnName = "id", columnDefinition = "NUMBER")
    private ModuleActions moduleActions;
}
