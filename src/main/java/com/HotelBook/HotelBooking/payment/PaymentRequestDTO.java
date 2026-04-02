package com.HotelBook.HotelBooking.payment;



import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public class PaymentRequestDTO {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;


    @NotNull(message = "simulateFailure is required (use false for real payment, true to test failure)")
    private Boolean simulateFailure;

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Boolean getSimulateFailure() { return simulateFailure; }
    public void setSimulateFailure(Boolean simulateFailure) { this.simulateFailure = simulateFailure; }
}
