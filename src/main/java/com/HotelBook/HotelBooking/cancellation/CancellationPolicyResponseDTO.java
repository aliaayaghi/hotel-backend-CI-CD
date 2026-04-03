package com.HotelBook.HotelBooking.cancellation;



import java.math.BigDecimal;
import java.util.UUID;


public class CancellationPolicyResponseDTO {

    private UUID id;
    private UUID hotelId;
    private UUID roomId;           // null = hotel-wide default
    private String tierName;
    private Integer deadlineHours;
    private Integer refundPercentage;
    private BigDecimal priceMultiplier;
    private Boolean isDefault;
    private String description;


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

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
