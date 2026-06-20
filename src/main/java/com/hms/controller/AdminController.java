package com.hms.controller;

import com.hms.dto.AdminUserCreateRequest;
import com.hms.dto.ApiResponse;
import com.hms.dto.AppointmentResponse;
import com.hms.dto.BillResponseDto;
import com.hms.dto.ChangePasswordRequest;
import com.hms.dto.DoctorAvailabilityRequest;
import com.hms.dto.DoctorRegisterRequest;
import com.hms.dto.DoctorResponse;
import com.hms.dto.GuestAppointmentRequest;
import com.hms.dto.PatientFullDetailsDto;
import com.hms.dto.UserProfileDto;
import com.hms.entity.DoctorAvailability;
import com.hms.entity.User;
import com.hms.service.AdminService;
import com.hms.service.BillingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final BillingService billingService;

    public AdminController(AdminService adminService, BillingService billingService) {
        this.adminService = adminService;
        this.billingService = billingService;
    }

    // --- Profile & Auth ---

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        return ResponseEntity.ok(adminService.getProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse> updateProfile(Authentication authentication, @RequestBody UserProfileDto dto) {
        adminService.updateProfile(authentication.getName(), dto);
        return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(Authentication authentication,
            @RequestBody ChangePasswordRequest request) {
        adminService.changePassword(authentication.getName(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
    }

    // --- User Management ---

    @PostMapping("/users/register")
    public ResponseEntity<ApiResponse> registerUser(@RequestBody AdminUserCreateRequest request,
            @RequestParam("role") String role) {
        adminService.registerAnyUser(request, role);
        return ResponseEntity.ok(new ApiResponse(true, role + " registered successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @GetMapping("/patients")
    public ResponseEntity<List<User>> getPatients() {
        return ResponseEntity.ok(adminService.getPatients());
    }

    @GetMapping("/patients/search")
    public ResponseEntity<User> searchPatient(@RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "mobile", required = false) String mobile) {
        if (id != null) {
            return ResponseEntity.ok(adminService.searchPatientById(id));
        } else if (mobile != null) {
            return ResponseEntity.ok(adminService.searchPatientByMobile(mobile));
        }
        throw new RuntimeException("Must provide id or mobile parameter to search");
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable("id") Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
    }

    @GetMapping("/patient/getfullDetails/{patientId}")
    public ResponseEntity<PatientFullDetailsDto> getPatientFullDetails(
            @PathVariable Long patientId) {

        PatientFullDetailsDto response = adminService.getPatientFullDetails(patientId);
        return ResponseEntity.ok(response);
    }

    // --- Appointment Management ---

    @GetMapping("/appointment-stats")
    public ResponseEntity<Map<String, Long>> getDoctorStats(Authentication auth) {

        return ResponseEntity.ok(
                adminService.getAppointmentStats());
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(adminService.getAllAppointments());
    }

    @GetMapping("/appointments/today")
    public ResponseEntity<List<AppointmentResponse>> getTodayAppointments() {
        return ResponseEntity.ok(adminService.getTodayAppointments());
    }

    @GetMapping("/appointments/future")
    public ResponseEntity<List<AppointmentResponse>> getFutureAppointments() {
        return ResponseEntity.ok(adminService.getFutureAppointments());
    }

    @GetMapping("/appointments/past")
    public ResponseEntity<List<AppointmentResponse>> getPastAppointments() {
        return ResponseEntity.ok(adminService.getPastAppointments());
    }

    @DeleteMapping("/appointments/{id}")
    public ResponseEntity<ApiResponse> deleteAppointment(@PathVariable("id") Long id) {
        adminService.deleteAppointment(id);
        return ResponseEntity.ok(new ApiResponse(true, "Appointment deleted successfully"));
    }

    @PostMapping("/appointments/guest")
    public ResponseEntity<ApiResponse> scheduleGuestAppointment(
            @RequestBody GuestAppointmentRequest request) {

        adminService.scheduleGuestAppointment(request);

        return ResponseEntity.ok(
                new ApiResponse(true, "Guest appointment scheduled successfully"));
    }

    // Register
    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponse> registerDoctor(@RequestBody DoctorRegisterRequest request) {
        return ResponseEntity.ok(adminService.registerDoctor(request));
    }

    // Get All
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(adminService.getAllDoctors());
    }

    // Get One
    @GetMapping("/doctors/{id}")
    public ResponseEntity<DoctorResponse> getDoctor(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.getDoctorById(id));
    }

    // Update
    @PutMapping("/doctors/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @RequestBody DoctorRegisterRequest request) {
        return ResponseEntity.ok(adminService.updateDoctor(id, request));
    }

    // Delete
    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<ApiResponse> deleteDoctor(@PathVariable Long id) {
        adminService.deleteDoctor(id);
        return ResponseEntity.ok(new ApiResponse(true, "Doctor deleted successfully"));
    }

    // CREATE / UPDATE
    @PostMapping("/doctor-availability")
    public ResponseEntity<?> save(@RequestBody DoctorAvailabilityRequest request) {
        adminService.saveAvailability(request);
        return ResponseEntity.ok("Saved");
    }

    // GET ALL
    @GetMapping("/doctor-slots")
    public ResponseEntity<?> getSlots(
            @RequestParam Long doctorId,
            @RequestParam String date) {

        return ResponseEntity.ok(adminService.getAvailableSlots(doctorId, date));
    }

    // GET BY DOCTOR
    @GetMapping("/doctor-availability/{doctorId}")
    public ResponseEntity<List<DoctorAvailability>> getByDoctor(
            @PathVariable Long doctorId) {
        return ResponseEntity.ok(adminService.getAvailabilityByDoctor(doctorId));
    }

    // DELETE
    @DeleteMapping("/doctor-availability/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        adminService.deleteAvailability(id);
        return ResponseEntity.ok("Deleted");
    }

    // ================= BILLING =================

    @GetMapping("/bills")
    public List<BillResponseDto> getAllBills() {
        return billingService.getAllBills();
    }

    @PutMapping("/bills/{id}/pay")
    public ResponseEntity<ApiResponse> payBill(@PathVariable Long id) {
        billingService.markAsPaid(id);
        return ResponseEntity.ok(new ApiResponse(true, "Bill paid successfully"));
    }
}
