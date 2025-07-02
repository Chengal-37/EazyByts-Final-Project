package com.newsaggregator.controller;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserPreference;
import com.newsaggregator.security.payload.response.MessageResponse;
import com.newsaggregator.security.services.UserDetailsImpl;
import com.newsaggregator.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust CORS for production
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Retrieves the profile of the authenticated user.
     * Requires user authentication.
     *
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with the User profile.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userService.getUserById(userDetails.getId());
            // It's good practice to map User entity to a DTO for responses to avoid exposing password hash etc.
            // For simplicity, returning User entity directly for now.
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving user profile: " + e.getMessage()));
        }
    }

    /**
     * Updates the profile of the authenticated user.
     * Requires user authentication.
     *
     * @param authentication The Spring Security Authentication object.
     * @param updatedUser The User object with updated details (username, email, firstName, lastName).
     * @return ResponseEntity with the updated User profile.
     */
    @PutMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> updateCurrentUserProfile(
            Authentication authentication,
            @Valid @RequestBody User updatedUser) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            User user = userService.updateUserProfile(userId, updatedUser);
            return ResponseEntity.ok(user);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating user profile: " + e.getMessage()));
        }
    }

    /**
     * Adds a preference for the authenticated user.
     * Requires user authentication.
     *
     * @param authentication The Spring Security Authentication object.
     * @param requestBody A map containing 'preferenceType' and 'preferenceValue'.
     * @return ResponseEntity with the created UserPreference.
     */
    @PostMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addUserPreference(
            Authentication authentication,
            @RequestBody Map<String, String> requestBody) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            String preferenceType = requestBody.get("preferenceType");
            String preferenceValue = requestBody.get("preferenceValue");

            if (preferenceType == null || preferenceType.isEmpty() || preferenceValue == null || preferenceValue.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Preference type and value cannot be empty."));
            }

            UserPreference preference = userService.addUserPreference(userId, preferenceType.toUpperCase(), preferenceValue);
            return ResponseEntity.status(HttpStatus.CREATED).body(preference);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error adding preference: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all preferences for the authenticated user.
     * Requires user authentication.
     *
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity with a list of UserPreference objects.
     */
    @GetMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getUserPreferences(Authentication authentication) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            List<UserPreference> preferences = userService.getUserPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving preferences: " + e.getMessage()));
        }
    }

    /**
     * Deletes a specific preference for the authenticated user.
     * Requires user authentication.
     *
     * @param authentication The Spring Security Authentication object.
     * @param preferenceType The type of preference to delete.
     * @param preferenceValue The value of the preference to delete.
     * @return ResponseEntity with a success message.
     */
    @DeleteMapping("/preferences")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteUserPreference(
            Authentication authentication,
            @RequestParam String preferenceType,
            @RequestParam String preferenceValue) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            userService.deleteUserPreference(userId, preferenceType.toUpperCase(), preferenceValue);
            return ResponseEntity.ok(new MessageResponse("Preference deleted successfully!"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting preference: " + e.getMessage()));
        }
    }
}
