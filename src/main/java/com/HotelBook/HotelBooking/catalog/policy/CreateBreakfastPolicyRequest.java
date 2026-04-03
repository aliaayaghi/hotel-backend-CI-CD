package com.HotelBook.HotelBooking.catalog.policy;


import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateBreakfastPolicyRequest {

    @NotNull(message = "breakfastOffered is required")
    private Boolean breakfastOffered;

    // true = free with room price
    private boolean includedInPrice;

    // null when includedInPrice=true or breakfastOffered=false
    @DecimalMin(value = "0.00", message = "Price per person cannot be negative")
    private BigDecimal pricePerPerson;

    // null when breakfastOffered=false
    private BreakfastType type;
}