package com.HotelBook.HotelBooking.catalog.policy;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateCheckInPolicyRequest {

    // HH:mm format — e.g. "14:00"
    @NotBlank(message = "Earliest check-in time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Time must be in HH:mm format, e.g. 14:00")
    private String earliestTime;

    // HH:mm format — e.g. "23:59"
    @NotBlank(message = "Latest check-in time is required")
    @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Time must be in HH:mm format, e.g. 23:59")
    private String latestTime;

    private boolean earlyCheckIn;

    private boolean lateCheckOut;

    // Optional additional policy notes
    private String description;
}
