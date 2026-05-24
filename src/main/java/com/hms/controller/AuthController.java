package com.hms.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hms.dto.ApiResponse;
import com.hms.dto.AuthResponse;
import com.hms.dto.ForgotPasswordRequest;
import com.hms.dto.LoginRequest;
import com.hms.dto.OperationResult;
import com.hms.dto.RegisterRequest;
import com.hms.dto.ResetPasswordRequest;
import com.hms.dto.VerifyOtpRequest;
import com.hms.service.ForgotPasswordService;
import com.hms.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UserService userService;
	private final ForgotPasswordService service;

	public AuthController(UserService userService, ForgotPasswordService service) {
		this.userService = userService;
		this.service = service;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse> registerUser(
			@RequestBody RegisterRequest request) {

		userService.registerUser(request);

		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new ApiResponse(true, "User registered successfully"));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(
			@RequestBody LoginRequest request) {

		AuthResponse response = userService.login(request);

		return ResponseEntity.ok(response);
	}

	@PostMapping("/forgot-password")
	public ResponseEntity<ApiResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		OperationResult result = service.sendOtp(request.getEmail());
		return ResponseEntity.ok(new ApiResponse(result.isSuccess(), result.getMessage()));
	}

	@PostMapping("/verify-otp")
	public ResponseEntity<ApiResponse> verifyOtp(
			@RequestBody VerifyOtpRequest request) {

		OperationResult result = service.verifyOtp(request.getEmail(), request.getOtp());
		return ResponseEntity.ok(new ApiResponse(result.isSuccess(), result.getMessage()));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<ApiResponse> resetPassword(
			@RequestBody ResetPasswordRequest request) {

		OperationResult result = service.resetPassword(request.getEmail(), request.getNewPassword());
		return ResponseEntity.ok(new ApiResponse(result.isSuccess(), result.getMessage()));
	}
}
