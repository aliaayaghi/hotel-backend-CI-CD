package com.HotelBook.HotelBooking.roomaccessibility;

import com.HotelBook.HotelBooking.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "room_accessibility",
        indexes = @Index(name = "idx_room_accessibility_room_id", columnList = "room_id"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoomAccessibility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 255)
    private String feature;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable;
}
