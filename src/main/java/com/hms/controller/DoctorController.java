package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.ChangePasswordRequest;
import com.hms.dto.CompleteAppointmentRequest;
import com.hms.dto.DoctorAppointmentDTO;
import com.hms.dto.DoctorProfileDto;
import com.hms.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/profile")
    public ResponseEntity<DoctorProfileDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(doctorService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(Authentication authentication, @RequestBody DoctorProfileDto dto) {
        doctorService.updateProfile(authentication.getName(), dto);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        doctorService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<DoctorAppointmentDTO>> getScheduledAppointments(Authentication authentication) {
        return ResponseEntity.ok(
                doctorService.getScheduledAppointments(authentication.getName()));
    }

    @GetMapping("/appointment-stats")
    public ResponseEntity<Map<String, Long>> getDoctorStats(Authentication auth) {

        return ResponseEntity.ok(
                doctorService.getDoctorAppointmentStats(auth.getName()));
    }

    @PutMapping("/appointments/{id}/complete")
    public ResponseEntity<ApiResponse> markAppointmentAsCompleted(
            Authentication authentication,
            @PathVariable("id") Long appointmentId,
            @RequestBody CompleteAppointmentRequest request) {
        doctorService.markAppointmentAsCompleted(authentication.getName(), appointmentId, request);
        return ResponseEntity.ok(new ApiResponse(true, "Appointment marked as completed"));
    }

}
