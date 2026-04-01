package com.HotelBook.catalog.photo;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class HotelPhotoResponse {

    private UUID id;
    private UUID hotelId;
    private String url;
    private String caption;
    private int order;
    private boolean isCover;
    private Instant uploadedAt;
}
