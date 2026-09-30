package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

/** "10. AMENITY" - master list of amenities (AC, Parking, Wi-Fi, Lift ...). */
@Entity
@Table(name = "ukm_tbl_amenities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 60)
    private String icon;
}
