package com.newsaggregator; // Ensure this matches your main application package

import com.newsaggregator.model.ERole;
import com.newsaggregator.model.Role;
import com.newsaggregator.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * CommandLineRunner to initialize default roles in the database on application startup.
 * This ensures that roles like ROLE_USER, ROLE_MODERATOR, and ROLE_ADMIN exist,
 * which are necessary for user registration and authorization.
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check and create ROLE_USER if it doesn't exist
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
            System.out.println("ROLE_USER created.");
        }

        // Check and create ROLE_MODERATOR if it doesn't exist
        if (roleRepository.findByName(ERole.ROLE_MODERATOR).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_MODERATOR));
            System.out.println("ROLE_MODERATOR created.");
        }

        // Check and create ROLE_ADMIN if it doesn't exist
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            System.out.println("ROLE_ADMIN created.");
        }
    }
}

