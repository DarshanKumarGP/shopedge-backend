package com.shopedge.backend.filters;

import com.shopedge.backend.entities.Role;
import com.shopedge.backend.entities.User;
import com.shopedge.backend.repositories.UserRepository;
import com.shopedge.backend.services.AuthService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@WebFilter(urlPatterns = {"/api/*", "/admin/*"})
@Component
public class AuthenticationFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);
    
    private final AuthService authService;
    private final UserRepository userRepository;
    
    // Updated to support both development and Docker environments
    private static final String[] ALLOWED_ORIGINS = {
        "http://localhost:3000",     // Development
        "http://localhost:5174",     // Vite dev server
        "http://localhost:5173",     // Vite dev server
        "http://localhost",          // Docker (port 80)
        "http://localhost:80",       // Docker (explicit port 80)
        "http://127.0.0.1:3000",     // Alternative localhost
        "http://127.0.0.1:5174",     // Alternative localhost
        "http://127.0.0.1:5173",     // Alternative localhost
        "http://127.0.0.1",          // Alternative localhost
        "http://127.0.0.1:80"        // Alternative localhost with port
    };
    
    // FIXED: Added correct register endpoint paths
    private static final String[] UNAUTHENTICATED_PATHS = {
        "/api/users/register",       // Your existing endpoint (if used)
        "/api/auth/register",        // FIXED: Added this missing path
        "/api/auth/login",           // Existing login path
        "/actuator/health"           // Health check endpoint
    };
    
    public AuthenticationFilter(AuthService authService, UserRepository userRepository) {
        System.out.println("AuthenticationFilter Started.");
        this.authService = authService;
        this.userRepository = userRepository;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            executeFilterLogic(request, response, chain);
        } catch (Exception e) {
            logger.error("Unexpected error in AuthenticationFilter", e);
            sendErrorResponse((HttpServletResponse) response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error");
        }
    }
    
    private void executeFilterLogic(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String requestURI = httpRequest.getRequestURI();
        
        logger.info("Request URI: {}", requestURI);
        
        // Allow unauthenticated paths
        if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
            logger.info("Public endpoint accessed: {}", requestURI);
            chain.doFilter(request, response);
            return;
        }
        
        // Handle preflight (OPTIONS) requests
        if (httpRequest.getMethod().equalsIgnoreCase("OPTIONS")) {
            setCORSHeaders(httpResponse, httpRequest);
            return;
        }
        
        // Extract and validate the token
        String token = getAuthTokenFromCookies(httpRequest);
        System.out.println("Extracted Token: " + token);
        
        if (token == null || !authService.validateToken(token)) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized: Invalid or missing token");
            return;
        }
        
        // Extract username and verify user
        String username = authService.extractUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        
        if (userOptional.isEmpty()) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED,
                    "Unauthorized: User not found");
            return;
        }
        
        // Get authenticated user and role
        User authenticatedUser = userOptional.get();
        Role role = authenticatedUser.getRole();
        
        logger.info("Authenticated User: {}, Role: {}", authenticatedUser.getUsername(), role);
        
        // Role-based access control
        if (requestURI.startsWith("/admin/") && role != Role.ADMIN) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN,
                    "Forbidden: Admin access required");
            return;
        }
        
        if (requestURI.startsWith("/api/") && role != Role.CUSTOMER && role != Role.ADMIN) {
            sendErrorResponse(httpResponse, HttpServletResponse.SC_FORBIDDEN,
                    "Forbidden: Customer access required");
            return;
        }
        
        // Attach user details to request
        httpRequest.setAttribute("authenticatedUser", authenticatedUser);
        chain.doFilter(request, response);
    }
    
    // UPDATED: Dynamic CORS headers based on request origin
    private void setCORSHeaders(HttpServletResponse response, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        
        // Check if origin is in allowed list
        if (origin != null && Arrays.asList(ALLOWED_ORIGINS).contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            // Fallback to localhost:3000 for development
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        }
        
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Timestamp, X-Requested-With");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, X-Timestamp");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String message)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
    }
    
    private String getAuthTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> "authToken".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }
}