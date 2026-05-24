package com.hms.service;

import com.hms.dto.AuthResponse;
import com.hms.dto.LoginRequest;
import com.hms.dto.RegisterRequest;
import com.hms.entity.Role;
import com.hms.entity.User;
import com.hms.repository.RoleRepository;
import com.hms.repository.UserRepository;
import com.hms.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;
	private final UserDetailsService userDetailsService;
	private final EmailService emailService;

	public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService,
			EmailService emailService) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.emailService = emailService;
	}

	public User registerUser(RegisterRequest request) {

		if (userRepository.findByEmail(request.getEmail()).isPresent()) {
			throw new RuntimeException("Email already exists");
		}

		if (userRepository.findByMobileNumber(request.getMobileNumber()).isPresent()) {
			throw new RuntimeException("Mobile number already exists");
		}

		User user = new User();
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setFirstName(request.getFirstName());
		user.setLastName(request.getLastName());
		user.setMobileNumber(request.getMobileNumber());

		Role role = roleRepository.findByName("PATIENT")
				.orElseThrow(() -> new RuntimeException("Role not found"));

		user.setRoles(Set.of(role));
		user.setEnabled(true);

		User savedUser = userRepository.save(user);
		emailService.sendRegistrationEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName(), role.getName());
		return savedUser;
	}

	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

		User user = userRepository.findByEmail(request.getEmail())
				.orElseThrow(() -> new RuntimeException("User not found"));

		if (!user.isEnabled()) {
			throw new RuntimeException("User account is disabled");
		}

		UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
		String jwtToken = jwtUtil.generateToken(userDetails);

		String roleName = user.getRoles().stream()
				.map(Role::getName)
				.findFirst()
				.orElse("UNKNOWN");

		return new AuthResponse(jwtToken, "Login successful", true, roleName,
				user.getFirstName() + " " + user.getLastName());
	}
}
