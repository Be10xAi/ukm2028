package com.tech10x.ukm.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ukm_tbl_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String name;   // ROLE_ADMIN, ROLE_EMPLOYEE

    @Column(length = 120)
    private String description;
    @Builder.Default
    @Column( name = "is_active")
    private Boolean isActive=true;


}