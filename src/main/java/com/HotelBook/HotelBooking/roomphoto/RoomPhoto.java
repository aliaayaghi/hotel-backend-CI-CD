package com.HotelBook.HotelBooking.roomphoto;

import com.HotelBook.HotelBooking.room.Room;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "room_photo",
        indexes = @Index(name = "idx_room_photo_room_id", columnList = "room_id"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RoomPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(length = 255)
    private String caption;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
