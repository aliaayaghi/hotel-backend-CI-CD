package com.HotelBook.HotelBooking.catalog.user.mapper;


import com.HotelBook.HotelBooking.catalog.user.dto.response.CustomerResponse;
import com.HotelBook.HotelBooking.catalog.user.dto.response.HotelManagerResponse;
import com.HotelBook.HotelBooking.catalog.user.dto.response.UserResponse;
import com.HotelBook.HotelBooking.catalog.user.entity.Customer;
import com.HotelBook.HotelBooking.catalog.user.entity.HotelManager;
import com.HotelBook.HotelBooking.catalog.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public CustomerResponse toCustomerResponse(User user, Customer customer) {
        return CustomerResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .phone(customer != null ? customer.getPhone() : null)
                .nationality(customer != null ? customer.getNationality() : null)
                .dateOfBirth(customer != null ? customer.getDateOfBirth() : null)
                .build();
    }

    public HotelManagerResponse toManagerResponse(User user, HotelManager manager) {
        return HotelManagerResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .phone(manager != null ? manager.getPhone() : null)
                .build();
    }
}
