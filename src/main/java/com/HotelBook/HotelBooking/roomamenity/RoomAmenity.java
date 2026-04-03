package com.HotelBook.HotelBooking.roomamenity;




import com.HotelBook.HotelBooking.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity
@Table(name = "room_amenity",
        indexes = @Index(name = "idx_room_amenity_room_id", columnList = "room_id"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoomAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 100)
    private String name;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AmenityCategory category;

    @Column(length = 100)
    private String icon;
}
