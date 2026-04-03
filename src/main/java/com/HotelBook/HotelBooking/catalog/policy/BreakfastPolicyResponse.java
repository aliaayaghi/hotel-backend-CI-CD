package com.HotelBook.HotelBooking.catalog.policy;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class BreakfastPolicyResponse {

    private UUID id;
    private boolean breakfastOffered;
    private boolean includedInPrice;

    // null if includedInPrice=true or breakfastOffered=false
    private BigDecimal pricePerPerson;

    // null if breakfastOffered=false
    private BreakfastType type;
}
