package com.HotelBook.HotelBooking.catalog.policy;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePetPolicyRequest {

    @NotNull(message = "petsAllowed is required")
    private Boolean petsAllowed;

    // null is valid when petsAllowed=false
    @DecimalMin(value = "0.00", message = "Pet fee cannot be negative")
    private BigDecimal petFee;
}
