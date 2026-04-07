package com.HotelBook.HotelBooking;



import com.HotelBook.HotelBooking.Security.JwtUtil;
import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pure unit tests for JwtUtil — no Spring context needed.
 * We inject fields via ReflectionTestUtils to mirror @Value injection.
 */
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

    // A valid 256-bit Base64-encoded secret (same format as the real one)
    private static final String TEST_SECRET =
            "/JhEGEJcqn5/R0gwGpcqDhYU+GKVfmzgmZrpLEH5Mus=";
    private static final long ONE_HOUR_MS = 3_600_000L;

    private JwtUtil jwtUtil;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expirationMs", ONE_HOUR_MS);

        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .email("alice@example.com")
                .password("hashed")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .build();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  generateToken
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("generateToken()")
    class GenerateToken {

        @Test
        @DisplayName("should generate a non-null, non-empty JWT string")
        void shouldGenerateNonEmptyToken() {
            String token = jwtUtil.generateToken(testUser);
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should generate a token with three dot-separated segments (header.payload.signature)")
        void shouldHaveThreeSegments() {
            String token = jwtUtil.generateToken(testUser);
            assertThat(token.split("\\.")).hasSize(3);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  extractUsername
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("extractUsername()")
    class ExtractUsername {

        @Test
        @DisplayName("should return the user's email as username")
        void shouldReturnEmail() {
            String token = jwtUtil.generateToken(testUser);
            assertThat(jwtUtil.extractUsername(token)).isEqualTo("alice@example.com");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  extractUserId
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("extractUserId()")
    class ExtractUserId {

        @Test
        @DisplayName("should return the UUID embedded in the token claims")
        void shouldReturnUserId() {
            String token = jwtUtil.generateToken(testUser);
            UUID extracted = jwtUtil.extractUserId(token);
            assertThat(extracted).isEqualTo(testUser.getId());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  extractRole
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("extractRole()")
    class ExtractRole {

        @Test
        @DisplayName("should return ROLE_CUSTOMER for a customer user")
        void shouldReturnRoleCustomer() {
            String token = jwtUtil.generateToken(testUser);
            assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_CUSTOMER");
        }

        @Test
        @DisplayName("should return ROLE_ADMIN for an admin user")
        void shouldReturnRoleAdmin() {
            User admin = User.builder()
                    .id(UUID.randomUUID())
                    .name("Admin")
                    .email("admin@hotel.com")
                    .password("hashed")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();

            String token = jwtUtil.generateToken(admin);
            assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_ADMIN");
        }

        @Test
        @DisplayName("should return ROLE_HOTEL_MANAGER for a hotel manager user")
        void shouldReturnRoleHotelManager() {
            User manager = User.builder()
                    .id(UUID.randomUUID())
                    .name("Manager")
                    .email("mgr@hotel.com")
                    .password("hashed")
                    .role(UserRole.HOTEL_MANAGER)
                    .isActive(true)
                    .build();

            String token = jwtUtil.generateToken(manager);
            assertThat(jwtUtil.extractRole(token)).isEqualTo("ROLE_HOTEL_MANAGER");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  isTokenValid
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("isTokenValid()")
    class IsTokenValid {

        @Test
        @DisplayName("should return true for a fresh token belonging to the same user")
        void shouldReturnTrueForValidToken() {
            String token = jwtUtil.generateToken(testUser);
            assertThat(jwtUtil.isTokenValid(token, testUser)).isTrue();
        }

        @Test
        @DisplayName("should return false when token belongs to a different user")
        void shouldReturnFalseForDifferentUser() {
            User otherUser = User.builder()
                    .id(UUID.randomUUID())
                    .name("Bob")
                    .email("bob@example.com")
                    .password("hashed")
                    .role(UserRole.CUSTOMER)
                    .isActive(true)
                    .build();

            String token = jwtUtil.generateToken(testUser);
            assertThat(jwtUtil.isTokenValid(token, otherUser)).isFalse();
        }

        @Test
        @DisplayName("should return false for an expired token")
        void shouldReturnFalseForExpiredToken() {
            // Set expiration to 1ms so the token expires immediately
            ReflectionTestUtils.setField(jwtUtil, "expirationMs", 1L);
            String token = jwtUtil.generateToken(testUser);

            // Small sleep to ensure it's expired
            try { Thread.sleep(10); } catch (InterruptedException ignored) {}

            assertThat(jwtUtil.isTokenValid(token, testUser)).isFalse();
        }

        @Test
        @DisplayName("should return false for a malformed token string")
        void shouldReturnFalseForMalformedToken() {
            assertThat(jwtUtil.isTokenValid("not.a.validtoken", testUser)).isFalse();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  isTokenExpired
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("isTokenExpired()")
    class IsTokenExpired {

        @Test
        @DisplayName("should return false for a freshly generated token")
        void shouldNotBeExpired() {
            String token = jwtUtil.generateToken(testUser);
            assertThat(jwtUtil.isTokenExpired(token)).isFalse();
        }


    }

    // ═════════════════════════════════════════════════════════════════════════
    //  validateTokenStructure
    // ═════════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("validateTokenStructure()")
    class ValidateTokenStructure {

        @Test
        @DisplayName("should return true for a valid, signed, non-expired token")
        void shouldReturnTrueForValidToken() {
            String token = jwtUtil.generateToken(testUser);
            assertThat(jwtUtil.validateTokenStructure(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for a randomly crafted string")
        void shouldReturnFalseForRandomString() {
            assertThat(jwtUtil.validateTokenStructure("random.garbage.string")).isFalse();
        }

        @Test
        @DisplayName("should return false for an empty string")
        void shouldReturnFalseForEmptyString() {
            assertThat(jwtUtil.validateTokenStructure("")).isFalse();
        }


    }
}

