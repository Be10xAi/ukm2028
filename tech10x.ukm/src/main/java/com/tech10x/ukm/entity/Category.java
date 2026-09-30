package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

/** "5. CATEGORY" - master list of property categories (Hotel, Dharamshala, Camp ...). */
@Entity
@Table(name = "ukm_tbl_categories")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    /** Unique, used in SEO URLs. */
    @Column(nullable = false, unique = true, length = 60)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;
}
