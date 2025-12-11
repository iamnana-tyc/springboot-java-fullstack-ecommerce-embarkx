package com.iamnana.project.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthEntryPointJwt  implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(AuthEntryPointJwt.class);

    /**
     * This method is automatically triggered whenever an unauthenticated user
     * tries to access a protected (secured) endpoint.
     *
     * In Spring Security, AuthenticationEntryPoint is responsible for handling
     * authentication errors — usually when:
     *   - No JWT token is provided
     *   - Token is expired
     *   - Token is invalid
     *   - User is not authenticated
     *
     * Instead of redirecting to a login page (default behavior),
     * we send a custom JSON response with error details.
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        // Log debug message for developers (not visible to end users)
        log.debug("Unauthorised error: {}", authException.getMessage());

        // Set response type to JSON so clients (like Postman or frontend apps) understand the format
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Set HTTP status: 401 means "Unauthorized"
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Create a body map that will become the JSON response sent back to the client
        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED); //eg. 401
        body.put("error", "Unauthorized"); // The error text
        body.put("message", authException.getMessage()); // Get the exact reason for failure (token issues, etc.)
        body.put("path", request.getServletPath()); // The endpoint user tried to access

        // Convert the Map into JSON and write it to the HTTP response output stream
        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
