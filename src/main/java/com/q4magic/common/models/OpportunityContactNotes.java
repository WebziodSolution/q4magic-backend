package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opportunity_contact_notes")
@Setter
@Getter
@NoArgsConstructor
public class OpportunityContactNotes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_contact_id", referencedColumnName = "id")
    private OpportunityContact opportunitiesContact;

    @Column(name = "type")
    private String type;

    @Column(name = "note")
    private String note;
}
