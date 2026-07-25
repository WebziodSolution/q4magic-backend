package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "opportunities_current_environment")
@Setter
@Getter
@NoArgsConstructor
public class OpportunitiesCurrentEnvironment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id  ", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @Column(name = "solution")
    private String solution;

    @Column(name = "vendors", columnDefinition = "longtext")
    private String vendors;
}
