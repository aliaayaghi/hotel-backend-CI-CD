package com.HotelBook.HotelBooking.savedhotel;


import jakarta.validation.constraints.Size;


public class SavedHotelRequestDTO {


    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
