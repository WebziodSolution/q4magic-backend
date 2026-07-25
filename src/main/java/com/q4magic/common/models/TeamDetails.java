package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "team_details")
@Setter
@Getter
@NoArgsConstructor
public class TeamDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "name")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name = "created_date")
    @Temporal(TemporalType.DATE)
    private Date registeredDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assign_member", referencedColumnName = "cus_id")
    private Customers assignMember;

    @Column(name = "assign_opp_id")
    private String assignedOpportunities;
}
