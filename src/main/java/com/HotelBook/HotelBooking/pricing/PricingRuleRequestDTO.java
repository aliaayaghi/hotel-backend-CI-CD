package com.HotelBook.HotelBooking.pricing;



import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;


public class PricingRuleRequestDTO {

    @NotNull(message = "Rule type is required")
    private PricingRule.RuleType ruleType;

    private LocalDate startDate;  // required for SEASONAL / SPECIAL_EVENT

    private LocalDate endDate;    // required for SEASONAL / SPECIAL_EVENT

    private String dayOfWeek;     // required for WEEKDAY_WEEKEND, e.g. "FRIDAY,SATURDAY"

    @NotNull(message = "Multiplier is required")
    @DecimalMin(value = "0.10", message = "Multiplier must be at least 0.10 (maximum 90% discount)")
    @DecimalMax(value = "10.00", message = "Multiplier cannot exceed 10.00 (10x price)")
    private BigDecimal multiplier;

    @Min(value = 0, message = "Priority cannot be negative")
    @Max(value = 100, message = "Priority cannot exceed 100")
    private Integer priority = 0;

    private Boolean isActive = true;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    // ── GETTERS & SETTERS ────────────────────────────────────────────────────

    public PricingRule.RuleType getRuleType() { return ruleType; }
    public void setRuleType(PricingRule.RuleType ruleType) { this.ruleType = ruleType; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public BigDecimal getMultiplier() { return multiplier; }
    public void setMultiplier(BigDecimal multiplier) { this.multiplier = multiplier; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
