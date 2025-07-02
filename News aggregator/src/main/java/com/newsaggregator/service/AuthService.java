package com.newsaggregator.service;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.exception.UserAlreadyExistsException;
import com.newsaggregator.model.ERole;
import com.newsaggregator.model.Role;
import com.newsaggregator.model.User;
import com.newsaggregator.repository.RoleRepository;
import com.newsaggregator.repository.UserRepository;
import com.newsaggregator.security.jwt.JwtUtils;
import com.newsaggregator.security.payload.request.LoginRequest;
import com.newsaggregator.security.payload.request.SignupRequest;
import com.newsaggregator.security.payload.response.JwtResponse;
import com.newsaggregator.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    /**
     * Authenticates a user and generates a JWT token.
     * @param loginRequest The login request containing username and password.
     * @return JwtResponse containing the JWT token and user details.
     */
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        // Authenticate the user using Spring Security's AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // Set the authentication in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate JWT token
        String jwt = jwtUtils.generateJwtToken(authentication);

        // Get user details from the authentication object
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Extract roles as a list of strings
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        // Return JWT response with token, user ID, username, email, and roles
        return new JwtResponse(jwt,
                               userDetails.getId(),
                               userDetails.getUsername(),
                               userDetails.getEmail(),
                               roles);
    }

    /**
     * Registers a new user.
     * @param signupRequest The signup request containing user details.
     * @return User object of the newly registered user.
     * @throws UserAlreadyExistsException if username or email already exists.
     */
    public User registerUser(SignupRequest signupRequest) {
        // Check if username already exists
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new UserAlreadyExistsException("Error: Username is already taken!");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new UserAlreadyExistsException("Error: Email is already in use!");
        }

        // Create new user's account
        User user = new User(signupRequest.getUsername(),
                             signupRequest.getEmail(),
                             encoder.encode(signupRequest.getPassword()), // Encode password
                             signupRequest.getFirstName(),
                             signupRequest.getLastName());

        Set<String> strRoles = signupRequest.getRole();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            // Default role is ROLE_USER if no roles are specified
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new ResourceNotFoundException("Error: Role 'USER' is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new ResourceNotFoundException("Error: Role 'ADMIN' is not found."));
                        roles.add(adminRole);
                        break;
                    case "mod":
                        Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new ResourceNotFoundException("Error: Role 'MODERATOR' is not found."));
                        roles.add(modRole);
                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new ResourceNotFoundException("Error: Role 'USER' is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);
        return userRepository.save(user); // Save the new user to the database
    }
}

