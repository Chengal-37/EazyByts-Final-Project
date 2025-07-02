package com.newsaggregator.security.config;

import com.newsaggregator.security.jwt.AuthEntryPointJwt;
import com.newsaggregator.security.jwt.AuthTokenFilter;
import com.newsaggregator.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity // Enables Spring Security method-level annotations like @PreAuthorize
public class WebSecurityConfig {

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;

    /**
     * Creates and returns a new instance of AuthTokenFilter.
     * This filter will be used to process JWT tokens in incoming requests.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Configures the DaoAuthenticationProvider, which uses UserDetailsService
     * and PasswordEncoder to authenticate users.
     * @return Configured DaoAuthenticationProvider.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService); // Set custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Set password encoder

        return authProvider;
    }

    /**
     * Provides the AuthenticationManager.
     * @param authConfig AuthenticationConfiguration to get the AuthenticationManager.
     * @return AuthenticationManager instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Provides the PasswordEncoder bean. Uses BCrypt for strong password hashing.
     * @return BCryptPasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the security filter chain.
     * Defines security rules for HTTP requests, CORS, session management, and adds JWT filter.
     * @param http HttpSecurity object to configure.
     * @return SecurityFilterChain instance.
     * @throws Exception if an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Disable CSRF as we are using token-based authentication
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS with custom configuration
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // Handle unauthorized access
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Set session policy to stateless (for JWT)
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**", "/favicon.ico","/error").permitAll()
                    .requestMatchers("/auth/**").permitAll() // Allow public access to authentication endpoints
                    .requestMatchers("/test/**").permitAll() // Example public endpoint (for testing)
                    .requestMatchers("/articles/**").permitAll() // Allow public access to news articles
                    .requestMatchers("/sources/**").permitAll() // Allow public access to sources
                    .requestMatchers("/categories/**").permitAll() 
                    .requestMatchers("/bookmarks/**").permitAll()
                    .requestMatchers("/comments/**").permitAll()
                    .requestMatchers("/users/**").permitAll() // Allow public access to categories
                    .anyRequest().authenticated() // All other requests require authentication
            );

        http.authenticationProvider(authenticationProvider()); // Set custom authentication provider

        // Add JWT token filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing).
     * Allows requests from your frontend origin (e.g., http://localhost:3000 or the Canvas URL).
     * You might need to adjust `setAllowedOrigins` to match your frontend's actual deployment URL.
     * @return CorsConfigurationSource instance.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Allow requests from all origins during development.
        // In production, restrict this to your frontend's domain(s).
        configuration.setAllowedOriginPatterns(List.of("*")); // Allows all origins, but safer to specify
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*")); // Allow all headers
        configuration.setAllowCredentials(true); // Allow sending credentials (cookies, auth headers)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Apply this CORS config to all paths
        return source;
    }
}

