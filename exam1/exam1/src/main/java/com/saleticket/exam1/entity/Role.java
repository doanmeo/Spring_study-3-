package com.saleticket.exam1.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name; // Bắt buộc phải có prefix ROLE_ (VD: ROLE_ADMIN, ROLE_USER)

    private String description;
}
