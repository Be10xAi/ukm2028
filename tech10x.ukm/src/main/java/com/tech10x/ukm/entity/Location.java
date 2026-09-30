package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/** "6. LOCATION" - master list of cities/areas shared by properties and search pages. */
@Entity
@Table(name = "ukm_tbl_locations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 80)
    private String city;

    @Column(nullable = false, length = 80)
    private String state;

    /** e.g. 'Near Mahakal Temple'. */
    @Column(name = "area_landmark", length = 120)
    private String areaLandmark;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;
}
