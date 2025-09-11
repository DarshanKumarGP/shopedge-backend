package com.shopedge.backend.controllers;

import com.shopedge.backend.DTO.LoginRequest;
import com.shopedge.backend.entities.User;
import com.shopedge.backend.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;
    
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, 
                                 HttpServletResponse response) {
        try {
            User user = authService.authenticate(loginRequest.getUsername(), 
                                               loginRequest.getPassword());
            String token = authService.generateToken(user);
            
            // Create HttpOnly cookie
            Cookie cookie = new Cookie("authToken", token);
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true if using HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(3600); // 1 hour
            cookie.setDomain("localhost");
            response.addCookie(cookie);
            
            // Optional but useful for debugging
            response.addHeader("Set-Cookie", 
                String.format("authToken=%s; HttpOnly; Path=/; Max-Age=3600; SameSite=None", token));
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "Login successful");
            responseBody.put("role", user.getRole().name());
            responseBody.put("username", user.getUsername());
            
            return ResponseEntity.ok(responseBody);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
