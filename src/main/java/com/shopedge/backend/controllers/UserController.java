package com.shopedge.backend.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopedge.backend.entities.User;
import com.shopedge.backend.services.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(
		  origins = {"http://localhost:3000", "http://localhost:5174", "http://localhost:5173","http://localhost", "http://localhost:80","http://127.0.0.1:3000", "http://127.0.0.1:5174", "http://127.0.0.1:5173","http://127.0.0.1", "http://127.0.0.1:80"},
		  allowCredentials = "true",
		  allowedHeaders = {"Content-Type", "Authorization", "X-Timestamp", "X-Requested-With"},
		  exposedHeaders = {"Authorization", "X-Timestamp"}
		)
public class UserController {
	
	private final UserService userService;
	
	@Autowired
	public UserController (UserService userService) {
		this.userService = userService;
	}
	
	@PostMapping("/register")
	public ResponseEntity<?> registerUser (@RequestBody User user) {
		try {
			User registeredUser = userService.registerUser(user);
			return ResponseEntity.ok(Map.of("message", "User registered successfully", "user", registeredUser));
			
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body (Map.of("error", e.getMessage()));
		}
	}
	
}