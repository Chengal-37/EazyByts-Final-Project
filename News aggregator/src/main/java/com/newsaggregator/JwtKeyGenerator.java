package com.newsaggregator;
import java.security.SecureRandom;
import java.util.Base64;

public class JwtKeyGenerator {

    public static void main(String[] args) {
        // Define the desired key length in bytes
        // For HS512 (HMAC-SHA512), the key size must be at least 512 bits.
        // 512 bits / 8 bits/byte = 64 bytes.
        int keyLengthBytes = 64; 

        // Generate cryptographically strong random bytes
        SecureRandom secureRandom = new SecureRandom();
        byte[] keyBytes = new byte[keyLengthBytes];
        secureRandom.nextBytes(keyBytes);

        // Base64 encode the bytes to get a string suitable for application.properties
        // *** CHANGE MADE HERE: Use Base64.getEncoder() for standard Base64, compatible with JJWT's Decoders.BASE64 ***
        String base64Key = Base64.getEncoder().encodeToString(keyBytes);

        System.out.println("Generated JWT Secret Key (Base64 Encoded):");
        System.out.println(base64Key);

        System.out.println("\nImportant: Copy this entire string and paste it into your");
        System.out.println("application.properties file for 'newsaggregator.app.jwtSecret'.");
        System.out.println("Ensure there are no extra spaces or newlines.");
    }
}
