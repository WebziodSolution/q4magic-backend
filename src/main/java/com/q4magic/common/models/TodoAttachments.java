package com.q4magic.common.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "todo_attachments")
@Setter
@Getter
@NoArgsConstructor
public class TodoAttachments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", referencedColumnName = "todo_id")
    private Todo todo;

    @Column(name = "type")
    private String type;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "path")
    private String path;

    @Column(name = "link_name")
    private String linkName;

    @Column(name = "link")
    private String link;
}
