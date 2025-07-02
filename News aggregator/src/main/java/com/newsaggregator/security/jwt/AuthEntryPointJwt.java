package com.newsaggregator.security.jwt;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        String path = request.getRequestURI();

        // Suppress logging for common harmless unauthenticated paths
        if (!path.equals("/error") && !path.equals("/favicon.ico")) {
            logger.error("Unauthorized error on {}: {}", path, authException.getMessage());
        }

        // Optional: add header for debugging
        response.setHeader("WWW-Authenticate", "Bearer realm=\"Access to protected resources\"");

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized");
    }
}
