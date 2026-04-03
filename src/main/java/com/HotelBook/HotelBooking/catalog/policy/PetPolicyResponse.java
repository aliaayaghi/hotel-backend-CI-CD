package com.HotelBook.HotelBooking.catalog.policy;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PetPolicyResponse {

    private UUID id;
    private boolean petsAllowed;

    // null if petsAllowed=false
    private BigDecimal petFee;
}
