package com.HotelBook.HotelBooking.catalog.user.dto.request;

import com.HotelBook.HotelBooking.catalog.user.enums.UserRole;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotNull(message = "Role is required")
    private UserRole role;

    // Optional — for CUSTOMER and HOTEL_MANAGER
    private String phone;

    // Optional — CUSTOMER only (ISO 3166-1 alpha-2)
    @Size(min = 2, max = 2, message = "Nationality must be a 2-letter country code")
    private String nationality;

    // Optional — CUSTOMER only
    private LocalDate dateOfBirth;
}
