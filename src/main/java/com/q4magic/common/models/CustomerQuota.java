package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "customer_quotas")
@Setter
@Getter
@NoArgsConstructor
public class CustomerQuota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cus_quo_id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name = "quota")
    private Integer quota;

    @Column(name = "term")
    private String term;

    @Column(name = "amount1")
    private Integer amount1;

    @Column(name = "amount2")
    private Integer amount2;

    @Column(name = "amount3")
    private Integer amount3;

    @Column(name = "amount4")
    private Integer amount4;

    @Column(name = "amount5")
    private Integer amount5;

    @Column(name = "amount6")
    private Integer amount6;

    @Column(name = "amount7")
    private Integer amount7;

    @Column(name = "amount8")
    private Integer amount8;

    @Column(name = "amount9")
    private Integer amount9;

    @Column(name = "amount10")
    private Integer amount10;

    @Column(name = "amount11")
    private Integer amount11;

    @Column(name = "amount12")
    private Integer amount12;
}
