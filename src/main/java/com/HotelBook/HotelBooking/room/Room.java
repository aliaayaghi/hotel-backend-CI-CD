package com.HotelBook.HotelBooking.room;



import com.HotelBook.HotelBooking.roomamenity.RoomAmenity;
import com.HotelBook.HotelBooking.roomphoto.RoomPhoto;
import com.hotelapp.roomaccessibility.RoomAccessibility;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Table(name = "room", indexes = {
        @Index(name = "idx_room_hotel_id",     columnList = "hotel_id"),
        @Index(name = "idx_room_is_active",    columnList = "is_active"),
        @Index(name = "idx_room_hotel_active", columnList = "hotel_id, is_active"),
        @Index(name = "idx_room_type",         columnList = "type"),
        @Index(name = "idx_room_bed_type",     columnList = "bed_type")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Room {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;


    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    @Column(name = "hotel_id", nullable = false, updatable = false)
    private UUID hotelId;


    @Column(nullable = false, length = 100)
    private String name;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoomType type;

    /** BedType enum values: KING, QUEEN, TWIN, DOUBLE, SINGLE, BUNK */
    @Enumerated(EnumType.STRING)
    @Column(name = "bed_type", nullable = false, length = 20)
    private BedType bedType;

    @Column(columnDefinition = "TEXT")
    private String description;


    @Column(name = "max_adults", nullable = false)
    private Integer maxAdults;

    @Column(name = "max_children", nullable = false)
    private Integer maxChildren;


    @Column(nullable = false)
    private Integer quantity;


    @Column(name = "size_sqm", precision = 6, scale = 2)
    private BigDecimal sizeSqm;

    @Column
    private Integer floor;


    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private RoomView view = RoomView.NONE;



    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;


    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;


    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<RoomPhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RoomAmenity> amenities = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RoomAccessibility> accessibilities = new ArrayList<>();




    public boolean canAccommodate(int adults, int children) {
        return adults <= this.maxAdults && children <= this.maxChildren;
    }


    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
