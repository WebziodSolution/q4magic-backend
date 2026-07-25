package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "subscription_rates")
@Setter
@Getter
@NoArgsConstructor
public class SubscriptionRates {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "license_type")
    private String licenseType;

    @Column(name = "amount")
    private Float amount;

    @Column(name = "begin_date")
    private Date beginDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "subscription_ratescol")
    private String subscriptionRatesCol;
}
