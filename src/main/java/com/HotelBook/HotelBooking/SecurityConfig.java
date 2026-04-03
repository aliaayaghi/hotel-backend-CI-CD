package com.HotelBook.HotelBooking;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * SecurityConfig — Spring Security configuration for the hotel booking API.
 *
 * ─── CURRENT STATE (DEVELOPMENT) ─────────────────────────────────────────────
 *
 * ALL requests are permitted without authentication.
 *
 * WHY: Member 1 owns the JWT/authentication system. Until their JWT filter
 * is delivered and integrated, this config allows the whole team to test
 * all endpoints freely without getting blocked by a 401 or login redirect.
 *
 * CSRF is disabled because this is a stateless REST API — CSRF protection is
 * only needed for browser-based form submissions with session cookies.
 * All our clients (Postman, frontend SPA) use Bearer tokens, not cookies.
 *
 * Session creation policy is STATELESS — no HttpSession is created or used.
 * Each request must be self-contained (no "remember me" sessions).
 *
 * ─── AFTER MEMBER 1 DELIVERS JWT ─────────────────────────────────────────────
 *
 * Replace the .authorizeHttpRequests(auth -> auth.anyRequest().permitAll()) block with:
 *
 *   auth
 *     .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/health").permitAll()
 *     .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
 *     .requestMatchers("/api/hotels/{hotelId}/bookings").hasRole("HOTEL_MANAGER")
 *     .anyRequest().authenticated()
 *
 * And add Member 1's JWT filter before UsernamePasswordAuthenticationFilter:
 *
 *   .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
 *
 * ─── WHY THIS BEAN IS REQUIRED ────────────────────────────────────────────────
 *
 * If you have spring-boot-starter-security on the classpath but NO SecurityConfig,
 * Spring Boot auto-configures a default setup that:
 *   1. Requires login for ALL requests (returns 401 without a session cookie)
 *   2. Generates a random password printed in the startup log
 *   3. Shows a login form on browser requests
 *
 * This SecurityConfig overrides that default and keeps everything open during dev.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain applied to all HTTP requests.
     *
     * Current settings:
     *   - CSRF disabled     → safe for stateless REST APIs
     *   - Sessions stateless → no HttpSession created
     *   - All requests permitted → no authentication required (dev mode)
     *
     * Swagger UI and Actuator health are explicitly permitted here so they
     * remain accessible even after JWT is added in production.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ── Disable CSRF — not needed for stateless REST APIs ──────────
                .csrf(AbstractHttpConfigurer::disable)

                // ── Stateless sessions — no HttpSession created or used ────────
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ── Request authorisation ──────────────────────────────────────
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI — always accessible (Member 3 needs this for docs)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/v3/api-docs"
                        ).permitAll()

                        // Actuator health — always accessible (deployment health checks)
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()

                        // ─────────────────────────────────────────────────────────
                        // ALL other endpoints are PERMITTED (no auth required).
                        //
                        // !! REPLACE THIS WITH ROLE-BASED RULES AFTER M1 JWT MERGE !!
                        //
                        // Example after JWT integration:
                        //   .requestMatchers(HttpMethod.POST, "/api/auth/**").permitAll()
                        //   .requestMatchers("/api/hotels/*/bookings").hasRole("HOTEL_MANAGER")
                        //   .anyRequest().authenticated()
                        // ─────────────────────────────────────────────────────────
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
