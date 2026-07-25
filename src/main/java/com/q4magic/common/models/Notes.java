package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notes")
@Setter
@Getter
@NoArgsConstructor
public class Notes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id", referencedColumnName = "meet_id")
    private Meetings meetings;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "background")
    private String background;

    @Column(name = "alignment")
    private String alignment;

    @Column(name = "agenda")
    private String agenda;
}
