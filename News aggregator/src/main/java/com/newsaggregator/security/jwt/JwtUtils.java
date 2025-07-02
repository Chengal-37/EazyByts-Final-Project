package com.newsaggregator.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Import SignatureException
import jakarta.annotation.PostConstruct; // Import PostConstruct from Jakarta
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${newsaggregator.app.jwtSecret}")
    private String jwtSecret;

    @Value("${newsaggregator.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private SecretKey key; // Store the SecretKey once it's initialized

    /**
     * Initializes the SecretKey for JWT signing/verification.
     * This method is called automatically by Spring after dependency injection
     * (including @Value properties like jwtSecret) has completed.
     * This prevents generating the key repeatedly and ensures jwtSecret is available.
     */
    @PostConstruct
    public void init() {
        try {
            // Decodes the Base64 secret string into bytes and creates an HMAC-SHA key.
            // Keys.hmacShaKeyFor automatically determines the appropriate HMAC algorithm
            // based on the key's length (e.g., HS512 for 64-byte key).
            this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
            logger.info("JWT Secret Key initialized successfully.");
        } catch (IllegalArgumentException e) {
            logger.error("Invalid JWT secret string format or length. Make sure it's a valid Base64 string and at least 512 bits (64 bytes) for HS512. Error: {}", e.getMessage());
            // Optionally, rethrow as a RuntimeException to prevent the application from starting with an invalid key
            throw new RuntimeException("Failed to initialize JWT secret key", e);
        } catch (Exception e) {
            logger.error("Unexpected error initializing JWT secret key: {}", e.getMessage());
            throw new RuntimeException("Unexpected error initializing JWT secret key", e);
        }
    }

    /**
     * Generates a JWT token for an authenticated user.
     * @param authentication The authentication object containing user details.
     * @return The generated JWT token string.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal(); // Changed to UserDetails for broader compatibility

        // Build the JWT token using modern JJWT API
        return Jwts.builder()
                .subject((userPrincipal.getUsername())) // Use .subject() method for the claim
                .issuedAt(new Date()) // Set issue date
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Set expiration date
                .signWith(this.key, Jwts.SIG.HS512) // Use the pre-initialized key and explicitly specify HS512
                .compact(); // Compact the token into a string
    }

    /**
     * Extracts the username from a JWT token.
     * @param token The JWT token string.
     * @return The username extracted from the token.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(this.key) // Use the pre-initialized key for verification
                .build()
                .parseSignedClaims(token) // Parse signed claims to get payload
                .getPayload() // Access the payload to get claims
                .getSubject();
    }

    /**
     * Validates a JWT token.
     * @param authToken The JWT token string to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                .verifyWith(this.key) // Use the pre-initialized key for verification
                .build()
                .parseSignedClaims(authToken); // Parse signed claims for validation
            return true;
        } catch (SignatureException e) { // Specific catch for signature issues (like key size)
            logger.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }
}
