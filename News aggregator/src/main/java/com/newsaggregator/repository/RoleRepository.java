package com.newsaggregator.repository;

import com.newsaggregator.model.ERole;
import com.newsaggregator.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Custom method to find a role by its name (ERole enum)
    Optional<Role> findByName(ERole name);
}

