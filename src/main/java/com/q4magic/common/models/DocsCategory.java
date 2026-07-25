package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "docs_category")
@Setter
@Getter
@NoArgsConstructor
public class DocsCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id  ", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opp_id", referencedColumnName = "opp_id")
    private Opportunities opportunities;

    @Column(name = "category_name")
    private String categoryName;
}
