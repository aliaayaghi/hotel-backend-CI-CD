package com.HotelBook.HotelBooking.search.controller;

import com.HotelBook.HotelBooking.common.pagination.PagedResponse;
import com.HotelBook.HotelBooking.search.dto.SearchRequestDTO;
import com.HotelBook.HotelBooking.search.dto.SearchResponseDTO;
import com.HotelBook.HotelBooking.search.service.SearchService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.HotelBook.HotelBooking.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@Validated
@Tag(name = "Search", description = "Hotel search with filters and autocomplete")
public class SearchController {

    private final SearchService searchService;

    /**
     * GET /api/hotels/search
     *
     * All filters are optional query parameters except city, checkIn, checkOut.
     *
     * Example:
     * GET /api/hotels/search?city=Dubai&checkIn=2025-12-24&checkOut=2025-12-27
     *      &adults=2&children=1&childrenAges=5
     *      &stars=4&stars=5
     *      &hotelType=RESORT
     *      &hotelAmenities=pool&hotelAmenities=gym
     *      &bedType=KING
     *      &freeCancellation=true&petsAllowed=true
     *      &sortBy=stars&sortOrder=desc
     *      &page=0&size=20
     */
    @GetMapping("/search")
    @Operation(
            summary = "Search hotels",
            description = "Search active hotels by city, dates, and optional filters. " +
                    "Returns only hotels with available rooms for the dates. " +
                    "Filters for room type, bed type, amenities, and policies are all optional."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Results returned successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Validation failed")
    })
    public ResponseEntity<ApiResponse<PagedResponse<SearchResponseDTO>>> search(
            @Valid @ModelAttribute SearchRequestDTO dto) {

        PagedResponse<SearchResponseDTO> results = searchService.search(dto);

        return ResponseEntity.ok(ApiResponse.success(
                results,
                "Found " + results.getTotalElements() + " hotel(s) matching your search"
        ));
    }

    /**
     * GET /api/hotels/autocomplete?q=Dub
     *
     * Returns up to 10 hotel names matching the query.
     * Used by the search bar for live suggestions.
     * Minimum 2 characters required.
     */
    @GetMapping("/autocomplete")
    @Operation(
            summary = "Autocomplete search",
            description = "Returns up to 10 hotel names matching the query. Minimum 2 characters."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200", description = "Suggestions returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400", description = "Query too short")
    })
    public ResponseEntity<ApiResponse<List<String>>> autocomplete(
            @RequestParam
            @NotBlank(message = "Query cannot be blank")
            @Size(min = 2, message = "Query must be at least 2 characters")
            String q) {

        List<String> suggestions = searchService.autocomplete(q);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}