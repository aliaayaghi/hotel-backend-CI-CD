package com.HotelBook.HotelBooking.catalog.user.dto.response;

import com.HotelBook.HotelBooking.catalog.user.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HotelManagerResponse {

    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private boolean isActive;
    private Instant createdAt;

    // Manager-specific fields
    private String phone;
}
