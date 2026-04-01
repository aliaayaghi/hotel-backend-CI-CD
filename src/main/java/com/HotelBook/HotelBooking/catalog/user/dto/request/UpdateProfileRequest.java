package com.HotelBook.HotelBooking.catalog.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProfileRequest {

    // All fields are optional — only non-null fields will be updated

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Size(min = 7, max = 20, message = "Phone must be between 7 and 20 characters")
    private String phone;

    @Size(min = 2, max = 2, message = "Nationality must be a 2-letter country code")
    private String nationality;

    private LocalDate dateOfBirth;
}
