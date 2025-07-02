package com.newsaggregator.repository;

import com.newsaggregator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Custom method to find a user by username
    Optional<User> findByUsername(String username);

    // Custom method to check if a user with a given username exists
    Boolean existsByUsername(String username);

    // Custom method to check if a user with a given email exists
    Boolean existsByEmail(String email);
}
