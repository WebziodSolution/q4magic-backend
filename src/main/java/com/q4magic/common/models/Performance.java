package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "performance")
@Setter
@Getter
@NoArgsConstructor
public class Performance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "new_mettings")
    private Integer newMettings;

    @Column(name = "old_mettings")
    private Integer oldMettings;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
    private Customers customers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cal_id", referencedColumnName = "id")
    private Calendar calendar;
}
