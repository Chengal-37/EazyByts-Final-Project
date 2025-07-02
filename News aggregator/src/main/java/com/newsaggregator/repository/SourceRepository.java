package com.newsaggregator.repository;

import com.newsaggregator.model.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SourceRepository extends JpaRepository<Source, Long> {
    // Custom method to find a source by its name
    Optional<Source> findByName(String name);

    // Custom method to check if a source with a given name exists
    Boolean existsByName(String name);
}
