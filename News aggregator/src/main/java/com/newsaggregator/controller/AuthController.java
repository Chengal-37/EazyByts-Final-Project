package com.newsaggregator.controller;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.exception.UserAlreadyExistsException;
import com.newsaggregator.security.payload.request.LoginRequest;
import com.newsaggregator.security.payload.request.SignupRequest;
import com.newsaggregator.security.payload.response.JwtResponse;
import com.newsaggregator.security.payload.response.MessageResponse;
import com.newsaggregator.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust CORS for production
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    /**
     * Handles user login and authentication.
     * @param loginRequest The request body containing username and password.
     * @return ResponseEntity with JWT token and user details on successful login,
     * or an error message if authentication fails.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.authenticateUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            // Catch more specific exceptions from Spring Security for better error messages if needed
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Error: Invalid username or password!"));
        }
    }

    /**
     * Handles user registration.
     * @param signupRequest The request body containing new user details.
     * @return ResponseEntity with a success message on successful registration,
     * or an error message if registration fails (e.g., username/email already taken).
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        try {
            authService.registerUser(signupRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponse(e.getMessage()));
        } catch (ResourceNotFoundException e) { // For role not found
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
