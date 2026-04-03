package com.HotelBook.HotelBooking.catalog.policy;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CheckInPolicyResponse {

    private UUID id;
    private String earliestTime;
    private String latestTime;
    private boolean earlyCheckIn;
    private boolean lateCheckOut;
    private String description;
}
