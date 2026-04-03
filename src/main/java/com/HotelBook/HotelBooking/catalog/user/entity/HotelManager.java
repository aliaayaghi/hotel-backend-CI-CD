package com.HotelBook.HotelBooking.catalog.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "hotel_managers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelManager {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "id")
    private User user;

    @Column(length = 20)
    private String phone;
}
