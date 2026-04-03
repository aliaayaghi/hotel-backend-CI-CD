package com.HotelBook.HotelBooking.catalog.admin;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SuspendUserRequest {

    @NotBlank(message = "Suspension reason is required")
    @Size(min = 10, max = 500, message = "Reason must be between 10 and 500 characters")
    private String reason;
}
