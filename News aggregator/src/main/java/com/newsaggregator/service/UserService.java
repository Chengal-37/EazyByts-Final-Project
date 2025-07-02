package com.newsaggregator.service;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserPreference;
import com.newsaggregator.repository.UserRepository;
import com.newsaggregator.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPreferenceRepository userPreferenceRepository;

    /**
     * Get a user by their ID.
     * @param userId The ID of the user.
     * @return The User object.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    /**
     * Get a user by their username.
     * @param username The username of the user.
     * @return The User object.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Add a new preference for a user.
     * @param userId The ID of the user.
     * @param preferenceType The type of preference (e.g., "CATEGORY", "KEYWORD").
     * @param preferenceValue The value of the preference (e.g., "Technology", "AI").
     * @return The saved UserPreference object.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws IllegalArgumentException if the preference already exists.
     */
    public UserPreference addUserPreference(Long userId, String preferenceType, String preferenceValue) {
        User user = getUserById(userId);

        // Check for existing preference to prevent duplicates based on unique constraint
        if (userPreferenceRepository.findByUserAndPreferenceTypeAndPreferenceValue(user, preferenceType, preferenceValue).isPresent()) {
            throw new IllegalArgumentException("Preference already exists for this user: " + preferenceType + " - " + preferenceValue);
        }

        UserPreference preference = new UserPreference(user, preferenceType, preferenceValue);
        return userPreferenceRepository.save(preference);
    }

    /**
     * Get all preferences for a specific user.
     * @param userId The ID of the user.
     * @return A list of UserPreference objects.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public List<UserPreference> getUserPreferences(Long userId) {
        User user = getUserById(userId);
        return userPreferenceRepository.findByUser(user);
    }

    /**
     * Delete a specific preference for a user.
     * @param userId The ID of the user.
     * @param preferenceType The type of preference to delete.
     * @param preferenceValue The value of the preference to delete.
     * @throws ResourceNotFoundException if the user or preference is not found.
     */
    public void deleteUserPreference(Long userId, String preferenceType, String preferenceValue) {
        User user = getUserById(userId);
        UserPreference preference = userPreferenceRepository.findByUserAndPreferenceTypeAndPreferenceValue(user, preferenceType, preferenceValue)
                .orElseThrow(() -> new ResourceNotFoundException("User preference not found."));
        userPreferenceRepository.delete(preference);
    }

    /**
     * Updates a user's profile information.
     * @param userId The ID of the user to update.
     * @param updatedUser The User object containing updated information.
     * @return The updated User object.
     * @throws ResourceNotFoundException if the user is not found.
     * @throws IllegalArgumentException if the updated username or email is already taken by another user.
     */
    public User updateUserProfile(Long userId, User updatedUser) {
        User existingUser = getUserById(userId);

        // Check if username is being changed and if new username is available
        if (!existingUser.getUsername().equals(updatedUser.getUsername()) && userRepository.existsByUsername(updatedUser.getUsername())) {
            throw new IllegalArgumentException("Username already taken: " + updatedUser.getUsername());
        }

        // Check if email is being changed and if new email is available
        if (!existingUser.getEmail().equals(updatedUser.getEmail()) && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + updatedUser.getEmail());
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        // Do NOT update password directly here; use a separate password change function

        return userRepository.save(existingUser);
    }

    /**
     * Retrieves all distinct preference categories for a given user.
     * This can be useful for dynamic UI population of user preferences.
     * @param userId The ID of the user.
     * @return A Set of distinct preference types (Strings).
     * @throws ResourceNotFoundException if the user is not found.
     */
    public Set<String> getDistinctPreferenceTypesForUser(Long userId) {
        User user = getUserById(userId);
        return userPreferenceRepository.findByUser(user).stream()
                .map(UserPreference::getPreferenceType)
                .collect(Collectors.toSet());
    }
}
