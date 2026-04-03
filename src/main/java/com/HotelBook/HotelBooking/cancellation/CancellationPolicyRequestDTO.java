package com.HotelBook.HotelBooking.cancellation;



import jakarta.validation.constraints.*;

import java.math.BigDecimal;


public class CancellationPolicyRequestDTO {

    @NotBlank(message = "Tier name is required")
    @Size(max = 100, message = "Tier name cannot exceed 100 characters")
    private String tierName;

    @NotNull(message = "Deadline hours is required")
    @Min(value = 0, message = "Deadline hours cannot be negative")
    @Max(value = 8760, message = "Deadline cannot exceed 8760 hours (1 year)")
    private Integer deadlineHours;

    @NotNull(message = "Refund percentage is required")
    @Min(value = 0, message = "Refund percentage cannot be negative")
    @Max(value = 100, message = "Refund percentage cannot exceed 100")
    private Integer refundPercentage;

    @NotNull(message = "Price multiplier is required")
    @DecimalMin(value = "1.00", message = "Price multiplier must be at least 1.00 (no discounts on cancellation policy)")
    @DecimalMax(value = "3.00", message = "Price multiplier cannot exceed 3.00")
    private BigDecimal priceMultiplier;

    private Boolean isDefault = false;

    @Size(max = 2000)
    private String description;


    public String getTierName() { return tierName; }
    public void setTierName(String tierName) { this.tierName = tierName; }

    public Integer getDeadlineHours() { return deadlineHours; }
    public void setDeadlineHours(Integer deadlineHours) { this.deadlineHours = deadlineHours; }

    public Integer getRefundPercentage() { return refundPercentage; }
    public void setRefundPercentage(Integer refundPercentage) { this.refundPercentage = refundPercentage; }

    public BigDecimal getPriceMultiplier() { return priceMultiplier; }
    public void setPriceMultiplier(BigDecimal priceMultiplier) { this.priceMultiplier = priceMultiplier; }

    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
