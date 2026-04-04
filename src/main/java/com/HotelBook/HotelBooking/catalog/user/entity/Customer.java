package com.HotelBook.HotelBooking.catalog.user.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(length = 20)
    private String phone;

    // ISO 3166-1 alpha-2  e.g. "PS", "US", "JO"
    @Column(length = 2)
    private String nationality;

    @Column
    private LocalDate dateOfBirth;


}
