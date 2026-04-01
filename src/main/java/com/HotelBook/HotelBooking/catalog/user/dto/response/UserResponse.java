package com.HotelBook.HotelBooking.catalog.user.dto.response;


import com.HotelBook.HotelBooking.catalog.user.enums.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private boolean isActive;
    private Instant createdAt;

    // password is NEVER included in any response
}
