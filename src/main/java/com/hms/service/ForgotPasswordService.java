package com.hms.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.hms.dto.OperationResult;
import com.hms.entity.PasswordResetOtp;
import com.hms.entity.User;
import com.hms.repository.PasswordResetOtpRepository;
import com.hms.repository.UserRepository;

@Service
public class ForgotPasswordService {

    private final PasswordResetOtpRepository otpRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public ForgotPasswordService(
            PasswordResetOtpRepository otpRepo,
            UserRepository userRepo,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public OperationResult sendOtp(String email) {

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = generateOtp();

        PasswordResetOtp entity = new PasswordResetOtp();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepo.save(entity);

        emailService.sendForgotPasswordOtpEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                otp);

        return new OperationResult(true, "OTP sent successfully");
    }

    public OperationResult verifyOtp(String email, String otp) {

        PasswordResetOtp data = otpRepo
                .findTopByEmailOrderByIdDesc(email)
                .orElseThrow();

        if (data.getExpiryTime().isBefore(LocalDateTime.now())) {
            return new OperationResult(false, "OTP expired");
        }

        if (!data.getOtp().equals(otp)) {
            return new OperationResult(false, "Invalid OTP");
        }

        data.setVerified(true);
        otpRepo.save(data);

        return new OperationResult(true, "OTP verified");
    }

    public OperationResult resetPassword(String email, String newPassword) {

        PasswordResetOtp data = otpRepo
                .findTopByEmailOrderByIdDesc(email)
                .orElseThrow();

        if (!data.isVerified()) {
            return new OperationResult(false, "OTP not verified");
        }

        User user = userRepo.findByEmail(email).orElseThrow();

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepo.save(user);

        otpRepo.delete(data);

        emailService.sendPasswordResetSuccessEmail(
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName());

        return new OperationResult(true, "Password reset successful");
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(999999));
    }
}