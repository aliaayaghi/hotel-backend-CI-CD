package com.HotelBook.catalog.photo;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderPhotosRequest {

    /**
     * Ordered list of photo UUIDs.
     *
     * The position of each UUID in this list becomes that photo's new display_order.
     * For example, if photoIds = [C, A, B]:
     *   - Photo C gets order = 0 (shown first)
     *   - Photo A gets order = 1
     *   - Photo B gets order = 2
     *
     * Rules enforced in HotelPhotoServiceImpl:
     *   - All UUIDs must belong to the specified hotelId.
     *   - Every existing photo for the hotel must appear in the list
     *     (no partial reorders — you must submit the full ordered list).
     *   - Duplicate UUIDs in the list are rejected.
     */
    @NotEmpty(message = "photoIds must contain at least one photo UUID")
    private List<UUID> photoIds;
}
