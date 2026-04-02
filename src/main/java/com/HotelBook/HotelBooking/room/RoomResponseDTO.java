package com.HotelBook.HotelBooking.room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


public class RoomResponseDTO {

    private UUID id;
    private UUID hotelId;
    private String name;
    private RoomType type;
    private BedType bedType;
    private String description;
    private Integer maxAdults;
    private Integer maxChildren;
    private Integer quantity;
    private BigDecimal sizeSqm;
    private Integer floor;
    private RoomView view;
    private BigDecimal price;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested collections — inline with the room for a complete response
    private List<PhotoDTO>           photos;
    private List<AmenityDTO>         amenities;
    private List<AccessibilityDTO>   accessibilities;
    private List<PricingRuleDTO>     pricingRules;            // NEW
    private List<CancellationPolicyDTO> cancellationPolicies; // NEW

    // ════════════════════════════════════════════════════════════════════════
    // NESTED DTOs
    // ════════════════════════════════════════════════════════════════════════

    public static class PhotoDTO {
        private UUID id;
        private String url;
        private Integer displayOrder;
        private String caption;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        public String getCaption() { return caption; }
        public void setCaption(String caption) { this.caption = caption; }
    }

    public static class AmenityDTO {
        private UUID id;
        private String name;
        private String category; // "TECH", "COMFORT", etc.
        private String icon;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }
    }

    public static class AccessibilityDTO {
        private UUID id;
        private String feature;
        private Boolean isAvailable;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getFeature() { return feature; }
        public void setFeature(String feature) { this.feature = feature; }
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
    }

    /**
     * NEW: PricingRuleDTO — embedded summary of a pricing rule.
     *
     * Gives the frontend enough info to display the pricing rules on the room
     * detail page: "Weekends +30%", "July–August +50%", etc.
     *
     * Full rule management is available via:
     *   GET /api/hotels/{hotelId}/rooms/{roomId}/pricing-rules
     */
    public static class PricingRuleDTO {
        private UUID id;
        private String ruleType;       // "WEEKDAY_WEEKEND", "SEASONAL", "SPECIAL_EVENT"
        private LocalDate startDate;   // null for WEEKDAY_WEEKEND
        private LocalDate endDate;     // null for WEEKDAY_WEEKEND
        private String dayOfWeek;      // "FRIDAY,SATURDAY" — null for date-range types
        private BigDecimal multiplier; // 1.30 = +30% surcharge
        private Integer priority;
        private Boolean isActive;
        private String description;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
        public String getRuleType() { return ruleType; }
        public void setRuleType(String ruleType) { this.ruleType = ruleType; }
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

    /**
     * NEW: CancellationPolicyDTO — embedded summary of a cancellation tier.
     *
     * Shows the customer which cancellation tiers are available for this room.
     * They choose one of these at booking time.
     *
     * Example data in the response:
     * [
     *   { "tierName": "Non-refundable",    "deadlineHours": 0,   "refundPercentage": 0,   "priceMultiplier": 1.00 },
     *   { "tierName": "Partial (48h)",     "deadlineHours": 48,  "refundPercentage": 50,  "priceMultiplier": 1.10 },
     *   { "tierName": "Free cancellation", "deadlineHours": 168, "refundPercentage": 100, "priceMultiplier": 1.25 }
     * ]
     *
     * NOTE: This only shows ROOM-SPECIFIC policies (from Room.cancellationPolicies).
     * Hotel-wide fallback policies are shown by CancellationPolicyController when
     * the room has no room-specific tiers.
     *
     * Full policy management via:
     *   GET /api/hotels/{hotelId}/rooms/{roomId}/cancellation-policies
     */
    public static class CancellationPolicyDTO {
        private UUID id;
        private String tierName;
        private Integer deadlineHours;
        private Integer refundPercentage;
        private BigDecimal priceMultiplier;
        private Boolean isDefault;
        private String description;

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }
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

    // ════════════════════════════════════════════════════════════════════════
    // GETTERS & SETTERS
    // ════════════════════════════════════════════════════════════════════════

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }

    public BedType getBedType() { return bedType; }
    public void setBedType(BedType bedType) { this.bedType = bedType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getMaxAdults() { return maxAdults; }
    public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

    public Integer getMaxChildren() { return maxChildren; }
    public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSizeSqm() { return sizeSqm; }
    public void setSizeSqm(BigDecimal sizeSqm) { this.sizeSqm = sizeSqm; }

    public Integer getFloor() { return floor; }
    public void setFloor(Integer floor) { this.floor = floor; }

    public RoomView getView() { return view; }
    public void setView(RoomView view) { this.view = view; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<PhotoDTO> getPhotos() { return photos; }
    public void setPhotos(List<PhotoDTO> photos) { this.photos = photos; }

    public List<AmenityDTO> getAmenities() { return amenities; }
    public void setAmenities(List<AmenityDTO> amenities) { this.amenities = amenities; }

    public List<AccessibilityDTO> getAccessibilities() { return accessibilities; }
    public void setAccessibilities(List<AccessibilityDTO> accessibilities) { this.accessibilities = accessibilities; }

    public List<PricingRuleDTO> getPricingRules() { return pricingRules; }
    public void setPricingRules(List<PricingRuleDTO> pricingRules) { this.pricingRules = pricingRules; }

    public List<CancellationPolicyDTO> getCancellationPolicies() { return cancellationPolicies; }
    public void setCancellationPolicies(List<CancellationPolicyDTO> cancellationPolicies) { this.cancellationPolicies = cancellationPolicies; }
}