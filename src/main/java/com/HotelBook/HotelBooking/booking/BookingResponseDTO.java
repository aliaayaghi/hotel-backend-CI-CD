package com.HotelBook.HotelBooking.booking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;


public class BookingResponseDTO {

    private UUID id;
    private UUID customerId;
    private UUID hotelId;
    private UUID roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer adults;
    private Integer children;
    private Integer roomCount;
    private long numberOfNights;
    private BigDecimal pricePerNight;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private String specialRequests;
    private LocalDateTime cancelledAt;
    private String cancelledBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Embedded summaries — avoids extra API calls on the client
    private RoomSummary room;                         // NEW
    private PaymentSummary payment;
    private CancellationTierSummary cancellationPolicy;


    public static class RoomSummary {
        private UUID roomId;
        private String roomName;
        private String roomType;   // "STANDARD", "SUITE", etc.
        private String bedType;    // "KING", "TWIN", etc.
        private Integer floor;
        private String view;       // "SEA", "CITY", etc.
        private BigDecimal basePrice;  // current base price (may differ from pricePerNight snapshot)
        private Integer quantity;

        public UUID getRoomId() { return roomId; }
        public void setRoomId(UUID roomId) { this.roomId = roomId; }
        public String getRoomName() { return roomName; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
        public String getBedType() { return bedType; }
        public void setBedType(String bedType) { this.bedType = bedType; }
        public Integer getFloor() { return floor; }
        public void setFloor(Integer floor) { this.floor = floor; }
        public String getView() { return view; }
        public void setView(String view) { this.view = view; }
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }


    public static class PaymentSummary {
        private UUID paymentId;
        private String status;
        private BigDecimal amount;
        private BigDecimal refundAmount;
        private String paymentMethod;
        private LocalDateTime paidAt;

        public UUID getPaymentId() { return paymentId; }
        public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public LocalDateTime getPaidAt() { return paidAt; }
        public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    }


    public static class CancellationTierSummary {
        private UUID policyId;
        private String tierName;
        private Integer deadlineHours;
        private Integer refundPercentage;
        private BigDecimal priceMultiplier;

        public UUID getPolicyId() { return policyId; }
        public void setPolicyId(UUID policyId) { this.policyId = policyId; }
        public String getTierName() { return tierName; }
        public void setTierName(String tierName) { this.tierName = tierName; }
        public Integer getDeadlineHours() { return deadlineHours; }
        public void setDeadlineHours(Integer deadlineHours) { this.deadlineHours = deadlineHours; }
        public Integer getRefundPercentage() { return refundPercentage; }
        public void setRefundPercentage(Integer refundPercentage) { this.refundPercentage = refundPercentage; }
        public BigDecimal getPriceMultiplier() { return priceMultiplier; }
        public void setPriceMultiplier(BigDecimal priceMultiplier) { this.priceMultiplier = priceMultiplier; }
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getHotelId() { return hotelId; }
    public void setHotelId(UUID hotelId) { this.hotelId = hotelId; }

    public UUID getRoomId() { return roomId; }
    public void setRoomId(UUID roomId) { this.roomId = roomId; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }

    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }

    public Integer getRoomCount() { return roomCount; }
    public void setRoomCount(Integer roomCount) { this.roomCount = roomCount; }

    public long getNumberOfNights() { return numberOfNights; }
    public void setNumberOfNights(long numberOfNights) { this.numberOfNights = numberOfNights; }

    public BigDecimal getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(BigDecimal pricePerNight) { this.pricePerNight = pricePerNight; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }

    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public RoomSummary getRoom() { return room; }
    public void setRoom(RoomSummary room) { this.room = room; }

    public PaymentSummary getPayment() { return payment; }
    public void setPayment(PaymentSummary payment) { this.payment = payment; }

    public CancellationTierSummary getCancellationPolicy() { return cancellationPolicy; }
    public void setCancellationPolicy(CancellationTierSummary cancellationPolicy) { this.cancellationPolicy = cancellationPolicy; }
}