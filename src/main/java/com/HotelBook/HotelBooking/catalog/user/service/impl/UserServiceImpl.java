package com.HotelBook.HotelBooking.catalog.user.service.impl;

import com.HotelBook.HotelBooking.catalog.user.dto.request.*;
import com.HotelBook.HotelBooking.catalog.user.dto.response.*;
import com.HotelBook.HotelBooking.catalog.user.entity.*;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.catalog.user.mapper.UserMapper;
import com.HotelBook.HotelBooking.catalog.user.repository.*;
import com.HotelBook.HotelBooking.catalog.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    // ── Get current user ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        User user = findUserById(userId);
        return userMapper.toUserResponse(user);
    }

    // ── Update profile ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = findUserById(userId);

        // Patch only non-null fields — leave existing values untouched
        if (request.getName() != null) {
            user.setName(request.getName());
        }

        user = userRepository.save(user);

        // Update role-specific fields based on user's role
        switch (user.getRole()) {
            case CUSTOMER -> updateCustomerFields(userId, request);
            case HOTEL_MANAGER -> updateManagerFields(userId, request);
            case ADMIN -> { /* Admin has no phone/nationality fields */ }
        }

        log.info("Profile updated for user: {}", user.getEmail());
        return userMapper.toUserResponse(user);
    }

    // ── Change password ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = findUserById(userId);

        // 1. Verify the old password matches what's stored
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        // 2. Verify the two new password fields match each other
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        // 3. Encode and save the new password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private void updateCustomerFields(UUID userId, UpdateProfileRequest request) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElse(null);

        if (customer == null) return;

        if (request.getPhone() != null)       customer.setPhone(request.getPhone());
        if (request.getNationality() != null)  customer.setNationality(request.getNationality());
        if (request.getDateOfBirth() != null)  customer.setDateOfBirth(request.getDateOfBirth());

        customerRepository.save(customer);
    }

    private void updateManagerFields(UUID userId, UpdateProfileRequest request) {
        HotelManager manager = hotelManagerRepository.findByUser_Id(userId)
                .orElse(null);

        if (manager == null) return;

        if (request.getPhone() != null) manager.setPhone(request.getPhone());

        hotelManagerRepository.save(manager);
    }
}
