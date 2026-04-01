package com.HotelBook.catalog.photo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CreatePhotoRequest {

    @NotBlank(message = "Photo URL is required")
    @URL(message = "url must be a valid URL (e.g. https://cdn.example.com/photo.webp)")
    @Size(max = 1000, message = "URL must not exceed 1000 characters")
    private String url;

    // Optional: displayed as image caption / alt text in the UI
    @Size(max = 255, message = "Caption must not exceed 255 characters")
    private String caption;

    // If true, this photo becomes the hotel's thumbnail in listing cards.
    // Any existing cover photo will have its isCover flag cleared automatically.
    private boolean isCover = false;
}
