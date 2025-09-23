package com.shopedge.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.shopedge.backend.entities.User;
import com.shopedge.backend.repositories.UserRepository;

@Service
public class UserService {
	
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	
	@Autowired
	public UserService (UserRepository userRepository) {
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}
	
	
	public User registerUser (User user) {
		if (userRepository.findByUsername (user.getUsername()).isPresent()) {
			throw new RuntimeException("Username is already taken");
		}
		
		if (userRepository.findByEmail (user.getEmail()).isPresent()) {
			throw new RuntimeException("Email is already registered");
		}
	
		user.setPassword (passwordEncoder.encode(user.getPassword()));
		return userRepository.save(user);
	}
	
	public User getUserById(Integer id) {
	    return userRepository.findById(id).orElse(null);
	}

}