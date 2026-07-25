package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "docs_attachments")
@Setter
@Getter
@NoArgsConstructor
public class DocsAttachments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id  ", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private DocsCategory docsCategory;

    @Column(name = "type")
    private String type;

    @Column(name = "link_name")
    private String linkName;

    @Column(name = "link")
    private String link;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "file_url")
    private String fileUrl;

}
