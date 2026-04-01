package com.HotelBook.HotelBooking.catalog.user.service.impl;


import com.HotelBook.HotelBooking.catalog.user.dto.request.*;
import com.HotelBook.HotelBooking.catalog.user.dto.response.*;
import com.HotelBook.HotelBooking.catalog.user.entity.*;
import com.HotelBook.HotelBooking.catalog.user.exception.DuplicateEmailException;
import com.HotelBook.HotelBooking.catalog.user.mapper.UserMapper;
import com.HotelBook.HotelBooking.catalog.user.repository.*;
import com.HotelBook.HotelBooking.catalog.user.service.AuthService;
import com.HotelBook.HotelBooking.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final HotelManagerRepository hotelManagerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    // ── Register ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Guard: reject if email already taken
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 2. Build and save the base User record
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .isActive(true)
                .build();

        user = userRepository.save(user);
        log.info("Registered new user: {} with role {}", user.getEmail(), user.getRole());

        // 3. Save role-specific record in its own table
        saveRoleSpecificRecord(user, request);

        // 4. Generate JWT and return
        String token = jwtUtil.generateToken(user);
        UserResponse userResponse = buildUserResponse(user, request);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userResponse)
                .build();
    }

    // ── Login ──────────────────────────────────────────────────────────────────

    @Override
    public AuthResponse login(LoginRequest request) {
        // 1. Delegate to Spring Security's AuthenticationManager.
        //    This internally calls CustomUserDetailsService.loadUserByUsername()
        //    and PasswordEncoder.matches() — throws AuthenticationException on failure.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. The principal is our User entity (it implements UserDetails)
        User user = (User) authentication.getPrincipal();
        log.info("User logged in: {}", user.getEmail());

        // 3. Generate JWT
        String token = jwtUtil.generateToken(user);

        // 4. Build response — fetch role-specific data for the response DTO
        UserResponse userResponse = buildUserResponseFromUser(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(userResponse)
                .build();
    }

    // ── Logout ─────────────────────────────────────────────────────────────────

    @Override
    public void logout(String token) {
        // JWT is stateless — for Step 1 this is a no-op.
        // The client simply discards the token.
        // For Step 2: add token to a Redis blacklist keyed on jti (JWT ID claim)
        //   with TTL = remaining token lifetime, so it auto-expires from the blacklist.
        log.info("Logout requested (stateless — token discarded by client)");
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * After saving the base User, save the role-specific table row.
     * Each role table shares the User's UUID as its primary key via @MapsId.
     */
    private void saveRoleSpecificRecord(User user, RegisterRequest request) {
        switch (user.getRole()) {
            case CUSTOMER -> {
                Customer customer = Customer.builder()
                        .user(user)
                        .phone(request.getPhone())
                        .nationality(request.getNationality())
                        .dateOfBirth(request.getDateOfBirth())
                        .build();
                customerRepository.save(customer);
            }
            case HOTEL_MANAGER -> {
                HotelManager manager = HotelManager.builder()
                        .user(user)
                        .phone(request.getPhone())
                        .build();
                hotelManagerRepository.save(manager);
            }
            case ADMIN -> {
                // Admins are created manually or via a seed script.
                // The register endpoint prevents ADMIN self-registration —
                // enforce this in the controller with:
                //   if (request.getRole() == UserRole.ADMIN) throw ForbiddenException
                Admin admin = Admin.builder()
                        .user(user)
                        .permissions("{}")
                        .build();
                adminRepository.save(admin);
            }
        }
    }

    /**
     * Build a UserResponse from the registration request data (avoids extra DB read).
     */
    private UserResponse buildUserResponse(User user, RegisterRequest request) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Build a UserResponse from a User entity (used after login).
     */
    private UserResponse buildUserResponseFromUser(User user) {
        return userMapper.toUserResponse(user);
    }
}
