package com.newsaggregator.repository;

import com.newsaggregator.model.User;
import com.newsaggregator.model.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    // Find all preferences for a specific user
    List<UserPreference> findByUser(User user);

    // Find preferences of a specific type for a specific user
    List<UserPreference> findByUserAndPreferenceType(User user, String preferenceType);

    // Find a specific preference by user, type, and value (for uniqueness check)
    Optional<UserPreference> findByUserAndPreferenceTypeAndPreferenceValue(User user, String preferenceType, String preferenceValue);

    // Delete a specific preference by user, type, and value
    void deleteByUserAndPreferenceTypeAndPreferenceValue(User user, String preferenceType, String preferenceValue);
}
