package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "business_info")
@Setter
@Getter
@NoArgsConstructor
public class BusinessInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id  ", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cus_id", referencedColumnName = "cus_id")
    private Customers customers;

    @Column(name = "business_name")
    private String businessName;

    @Column(name = "brand_logo")
    private String brandLogo;

    @Column(name = "website_url")
    private String websiteUrl;
}
