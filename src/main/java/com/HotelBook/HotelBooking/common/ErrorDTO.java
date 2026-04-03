package com.HotelBook.HotelBooking.common;


import java.time.LocalDateTime;
import java.util.List;


public class ErrorDTO {

    private int status;
    private LocalDateTime timestamp;
    private List<FieldError> errors;

    public ErrorDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorDTO(int status, List<FieldError> errors) {
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }


    public static class FieldError {
        private String field;
        private String message;

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }


    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public List<FieldError> getErrors() { return errors; }
    public void setErrors(List<FieldError> errors) { this.errors = errors; }
}
