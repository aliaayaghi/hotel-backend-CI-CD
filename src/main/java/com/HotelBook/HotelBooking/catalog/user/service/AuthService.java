package com.HotelBook.HotelBooking.catalog.user.service;

import com.HotelBook.HotelBooking.catalog.user.dto.request.LoginRequest;
import com.HotelBook.HotelBooking.catalog.user.dto.request.RegisterRequest;
import com.HotelBook.HotelBooking.catalog.user.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);
}
