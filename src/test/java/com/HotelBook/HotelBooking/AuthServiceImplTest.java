package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Common.exception.DuplicateEmailException;
import com.HotelBook.HotelBooking.Security.JwtUtil;
import com.HotelBook.HotelBooking.User.dto.request.LoginRequest;
import com.HotelBook.HotelBooking.User.dto.request.RegisterRequest;
import com.HotelBook.HotelBooking.User.dto.response.AuthResponse;
import com.HotelBook.HotelBooking.User.entity.Admin;
import com.HotelBook.HotelBooking.User.entity.Customer;
import com.HotelBook.HotelBooking.User.entity.HotelManager;
import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.enums.UserRole;
import com.HotelBook.HotelBooking.User.mapper.UserMapper;
import com.HotelBook.HotelBooking.User.repository.AdminRepository;
import com.HotelBook.HotelBooking.User.repository.CustomerRepository;
import com.HotelBook.HotelBooking.User.repository.HotelManagerRepository;
import com.HotelBook.HotelBooking.User.repository.UserRepository;
import com.HotelBook.HotelBooking.User.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private HotelManagerRepository hotelManagerRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setName("Alice");
        registerRequest.setEmail("alice@example.com");
        registerRequest.setPassword("secret123");
        registerRequest.setRole(UserRole.CUSTOMER);
        registerRequest.setPhone("+970599000000");

        savedUser = User.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .email("alice@example.com")
                .password("hashed")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  register
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should register a CUSTOMER and return token")
        void shouldRegisterCustomerSuccessfully() {
            given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("hashed");
            given(userRepository.save(any(User.class))).willReturn(savedUser);
            given(jwtUtil.generateToken(any(User.class))).willReturn("jwt-token");

            AuthResponse response = authService.register(registerRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUser()).isNotNull();
            verify(customerRepository).save(any(Customer.class));
        }

        @Test
        @DisplayName("should register a HOTEL_MANAGER and save manager record")
        void shouldRegisterHotelManagerSuccessfully() {
            registerRequest.setRole(UserRole.HOTEL_MANAGER);
            User managerUser = User.builder()
                    .id(UUID.randomUUID())
                    .name("Alice")
                    .email("alice@example.com")
                    .password("hashed")
                    .role(UserRole.HOTEL_MANAGER)
                    .isActive(true)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("hashed");
            given(userRepository.save(any())).willReturn(managerUser);
            given(jwtUtil.generateToken(any())).willReturn("jwt-token");

            authService.register(registerRequest);

            verify(hotelManagerRepository).save(any(HotelManager.class));
            verify(customerRepository, never()).save(any());
        }

        @Test
        @DisplayName("should register an ADMIN and save admin record")
        void shouldRegisterAdminSuccessfully() {
            registerRequest.setRole(UserRole.ADMIN);
            User adminUser = User.builder()
                    .id(UUID.randomUUID())
                    .name("Alice")
                    .email("alice@example.com")
                    .password("hashed")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();

            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("hashed");
            given(userRepository.save(any())).willReturn(adminUser);
            given(jwtUtil.generateToken(any())).willReturn("jwt-token");

            authService.register(registerRequest);

            verify(adminRepository).save(any(Admin.class));
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email already exists")
        void shouldThrowWhenEmailAlreadyExists() {
            given(userRepository.existsByEmail(registerRequest.getEmail())).willReturn(true);

            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(DuplicateEmailException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should encode password before saving")
        void shouldEncodePassword() {
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode("secret123")).willReturn("$2a$bcrypt$hashed");
            given(userRepository.save(any())).willReturn(savedUser);
            given(jwtUtil.generateToken(any())).willReturn("tok");

            authService.register(registerRequest);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("$2a$bcrypt$hashed");
        }

        @Test
        @DisplayName("should set isActive=true on registration")
        void shouldSetUserActive() {
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("hashed");
            given(userRepository.save(any())).willReturn(savedUser);
            given(jwtUtil.generateToken(any())).willReturn("tok");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            authService.register(registerRequest);
            verify(userRepository).save(captor.capture());

            assertThat(captor.getValue().isActive()).isTrue();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  login
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return token on successful login")
        void shouldReturnTokenOnLogin() {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("alice@example.com");
            loginRequest.setPassword("secret123");

            Authentication auth = mock(Authentication.class);
            given(auth.getPrincipal()).willReturn(savedUser);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(auth);
            given(jwtUtil.generateToken(savedUser)).willReturn("jwt-token");
            given(userMapper.toUserResponse(savedUser)).willReturn(
                    com.HotelBook.HotelBooking.User.dto.response.UserResponse.builder()
                            .id(savedUser.getId())
                            .name(savedUser.getName())
                            .email(savedUser.getEmail())
                            .role(savedUser.getRole())
                            .isActive(true)
                            .build());

            AuthResponse response = authService.login(loginRequest);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("should propagate AuthenticationException on bad credentials")
        void shouldThrowOnBadCredentials() {
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setEmail("alice@example.com");
            loginRequest.setPassword("wrongPassword");

            given(authenticationManager.authenticate(any()))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  logout
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("should execute without throwing (stateless no-op)")
        void shouldNotThrow() {
            // Stateless JWT logout — just a no-op
            authService.logout("any-token");
            // No exception = pass
        }
    }
}

