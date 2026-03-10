package com.HotelBook.security;


import com.HotelBook.catalog.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;



    @Slf4j
    @Component
    public class JwtUtil {

        @Value("${jwt.secret}")
        private String secretKey;

        @Value("${jwt.expiration:86400000}")
        private long expirationMs;


        public String generateToken(UserDetails userDetails) {
            return generateToken(new HashMap<>(), userDetails);
        }


        public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
            extraClaims.put("role", userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse(""));


            if (userDetails instanceof User user) {
                extraClaims.put("userId", user.getId().toString());
            }

            return Jwts.builder()
                    .setClaims(extraClaims)
                    .setSubject(userDetails.getUsername())   // username = email
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        }


        public String extractUsername(String token) {
            return extractClaim(token, Claims::getSubject);
        }


        public UUID extractUserId(String token) {
            String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
            return userIdStr != null ? UUID.fromString(userIdStr) : null;
        }

        /**
         * Extract the role string (e.g. "ROLE_ADMIN") from the token claims.
         */
        public String extractRole(String token) {
            return extractClaim(token, claims -> claims.get("role", String.class));
        }


        public boolean isTokenValid(String token, UserDetails userDetails) {
            try {
                final String username = extractUsername(token);
                return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            } catch (Exception e) {
                log.warn("Token validation failed: {}", e.getMessage());
                return false;
            }
        }


        public boolean isTokenExpired(String token) {
            return extractExpiration(token).before(new Date());
        }


        public boolean validateTokenStructure(String token) {
            try {
                Jwts.parserBuilder()
                        .setSigningKey(getSigningKey())
                        .build()
                        .parseClaimsJws(token);
                return true;
            } catch (MalformedJwtException e) {
                log.warn("Invalid JWT token: {}", e.getMessage());
            } catch (ExpiredJwtException e) {
                log.warn("JWT token is expired: {}", e.getMessage());
            } catch (UnsupportedJwtException e) {
                log.warn("JWT token is unsupported: {}", e.getMessage());
            } catch (IllegalArgumentException e) {
                log.warn("JWT claims string is empty: {}", e.getMessage());
            }
            return false;
        }


        private Date extractExpiration(String token) {
            return extractClaim(token, Claims::getExpiration);
        }


        public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
            final Claims claims = extractAllClaims(token);
            return claimsResolver.apply(claims);
        }

        private Claims extractAllClaims(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }


        private Key getSigningKey() {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

