package com.HotelBook.HotelBooking.catalog.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "admins")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    // Use plain TEXT instead of JSON for MySQL 5.x compatibility
    @Column(columnDefinition = "TEXT")
    private String permissions;
}