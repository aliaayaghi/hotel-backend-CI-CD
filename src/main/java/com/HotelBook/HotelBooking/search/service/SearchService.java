package com.HotelBook.HotelBooking.search.service;


import com.HotelBook.HotelBooking.common.pagination.PagedResponse;
import com.HotelBook.HotelBooking.search.dto.SearchRequestDTO;
import com.HotelBook.HotelBooking.search.dto.SearchResponseDTO;

import java.util.List;

public interface SearchService {

    PagedResponse<SearchResponseDTO> search(SearchRequestDTO dto);

    List<String> autocomplete(String query);
}