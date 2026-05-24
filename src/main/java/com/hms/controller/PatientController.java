package com.hms.controller;

import com.hms.dto.ApiResponse;
import com.hms.dto.AppointmentRequestDto;
import com.hms.dto.AppointmentResponse;
import com.hms.dto.BillResponseDto;
import com.hms.dto.ChangePasswordRequest;
import com.hms.dto.DoctorResponse;
import com.hms.dto.UserProfileDto;
import com.hms.entity.Bill;
import com.hms.entity.DoctorAvailability;
import com.hms.entity.User;
import com.hms.service.AdminService;
import com.hms.service.BillingService;
import com.hms.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;
    private final AdminService adminService;
    private final BillingService billService;

    public PatientController(PatientService patientService, AdminService adminService, BillingService billService) {
        this.patientService = patientService;
        this.adminService = adminService;
        this.billService = billService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(patientService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(Authentication authentication, @RequestBody UserProfileDto dto) {
        patientService.updateProfile(authentication.getName(), dto);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        patientService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    // @GetMapping("/doctors")
    // public ResponseEntity<List<User>> getAvailableDoctors() {
    // // Exposing raw User entity is generally not recommended, but sufficient for
    // // quick implementation
    // // Normally we'd use a DoctorDto.
    // return ResponseEntity.ok(patientService.getAvailableDoctors());
    // }

    @PostMapping("/appointments")
    public ResponseEntity<ApiResponse> bookAppointment(Authentication authentication,
            @RequestBody AppointmentRequestDto request) {
        patientService.bookAppointment(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Appointment booked successfully"));
    }

    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<ApiResponse> cancelAppointment(Authentication authentication,
            @PathVariable("id") Long appointmentId) {
        patientService.cancelAppointment(appointmentId, authentication.getName());
        return ResponseEntity.ok(new ApiResponse(true, "Appointment cancelled successfully"));
    }

    @PutMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse> editAppointment(Authentication authentication,
            @PathVariable("id") Long appointmentId, @RequestBody AppointmentRequestDto request) {
        patientService.editAppointment(appointmentId, request, authentication.getName());
        return ResponseEntity.ok(new ApiResponse(true, "Appointment updated successfully"));
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentHistory(
            Authentication authentication,
            @RequestParam(value = "status", required = false) String status) {

        return ResponseEntity.ok(
                patientService.getAppointmentHistory(authentication.getName(), status));
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    // Get One
    @GetMapping("/doctors/{id}")
    public ResponseEntity<DoctorResponse> getDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getDoctorById(id));
    }

    @GetMapping("/doctor-slots")
    public ResponseEntity<?> getSlots(
            @RequestParam Long doctorId,
            @RequestParam String date) {

        return ResponseEntity.ok(patientService.getAvailableSlots(doctorId, date));
    }

    @GetMapping("/my-bills")
    public List<BillResponseDto> getMyBills(Authentication authentication) {

        User user = patientService.getUserByEmail(authentication.getName());

        return billService.getPatientBills(user.getId());
    }

    /* ================= PAY BILL ================= */

    @PutMapping("/pay/{billId}")
    public ResponseEntity<ApiResponse> payBill(@PathVariable Long billId,
            Authentication authentication) {

        User user = patientService.getUserByEmail(authentication.getName());

        billService.payBillByPatient(billId, user.getId());

        return ResponseEntity.ok(new ApiResponse(true, "Payment successful"));
    }

    // GET BY DOCTOR

    @GetMapping("/doctor-availability/{doctorId}")
    public ResponseEntity<List<DoctorAvailability>> getByDoctor(
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(adminService.getAvailabilityByDoctor(doctorId));
    }

}
