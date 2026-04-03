package com.HotelBook.HotelBooking.roomavailability;



import java.time.LocalDate;


public class AvailabilitySummaryResponseDTO {


    private LocalDate date;
    private Integer availableCount;
    private Integer blockedCount;
    private Boolean fullyBooked;
    private String blockedReason;


    public AvailabilitySummaryResponseDTO() {}

    public AvailabilitySummaryResponseDTO(LocalDate date, int availableCount,
                                          int blockedCount, boolean fullyBooked,
                                          String blockedReason) {
        this.date = date;
        this.availableCount = availableCount;
        this.blockedCount = blockedCount;
        this.fullyBooked = fullyBooked;
        this.blockedReason = blockedReason;
    }


    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getAvailableCount() { return availableCount; }
    public void setAvailableCount(Integer availableCount) { this.availableCount = availableCount; }

    public Integer getBlockedCount() { return blockedCount; }
    public void setBlockedCount(Integer blockedCount) { this.blockedCount = blockedCount; }

    public Boolean getFullyBooked() { return fullyBooked; }
    public void setFullyBooked(Boolean fullyBooked) { this.fullyBooked = fullyBooked; }

    public String getBlockedReason() { return blockedReason; }
    public void setBlockedReason(String blockedReason) { this.blockedReason = blockedReason; }
}
