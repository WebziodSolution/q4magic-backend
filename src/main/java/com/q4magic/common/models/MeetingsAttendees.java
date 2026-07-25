package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "meeting_attendees")
@Setter
@Getter
@NoArgsConstructor
public class MeetingsAttendees {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "meet_id_att", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_id", referencedColumnName = "meet_id")
    private Meetings meetings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "con_id", referencedColumnName = "con_id")
    private Contacts contacts;

    @Column(name = "title")
    private String title;

    @Column(name = "role")
    private String role;

    @Column(name = "note")
    private String note;
}
