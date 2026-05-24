package com.hms.service;

import com.hms.dto.AppointmentResponse;
import com.hms.dto.BillItemDto;
import com.hms.dto.BillResponseDto;
import com.hms.dto.ChangePasswordRequest;
import com.hms.dto.DoctorAvailabilityRequest;
import com.hms.dto.DoctorRegisterRequest;
import com.hms.dto.DoctorResponse;
import com.hms.dto.GuestAppointmentRequest;
import com.hms.dto.PatientFullDetailsDto;
import com.hms.dto.RegisterRequest;
import com.hms.dto.UserProfileDto;
import com.hms.entity.Appointment;
import com.hms.entity.DoctorAvailability;
import com.hms.entity.DoctorDetails;
import com.hms.entity.Role;
import com.hms.entity.User;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorAvailabilityRepository;
import com.hms.repository.DoctorDetailsRepository;
import com.hms.repository.RoleRepository;
import com.hms.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final DoctorDetailsRepository doctorDetailsRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private BillingService billingService;

    public AdminService(UserRepository userRepository, RoleRepository roleRepository,
            AppointmentRepository appointmentRepository, PasswordEncoder passwordEncoder,
            EmailService emailService, DoctorDetailsRepository doctorDetailsRepository,
            DoctorAvailabilityRepository doctorAvailability) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.doctorDetailsRepository = doctorDetailsRepository;
        this.availabilityRepository = doctorAvailability;
    }

    // --- Profile & Auth ---

    private User getAdminByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Admin not found"));
    }

    public UserProfileDto getProfile(String email) {
        User admin = getAdminByEmail(email);
        UserProfileDto dto = new UserProfileDto();
        dto.setFirstName(admin.getFirstName());
        dto.setLastName(admin.getLastName());
        dto.setMobileNumber(admin.getMobileNumber());
        dto.setEmail(admin.getEmail());
        return dto;
    }

    public void updateProfile(String email, UserProfileDto dto) {
        User admin = getAdminByEmail(email);
        admin.setFirstName(dto.getFirstName());
        admin.setLastName(dto.getLastName());
        admin.setMobileNumber(dto.getMobileNumber());
        userRepository.save(admin);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User admin = getAdminByEmail(email);
        if (!passwordEncoder.matches(request.getOldPassword(), admin.getPassword())) {
            throw new RuntimeException("Incorrect current password");
        }
        admin.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(admin);
        emailService.sendPasswordChangeEmail(admin.getEmail(), admin.getFirstName());
    }

    // --- User Management ---

    public User registerAnyUser(RegisterRequest request, String roleName) {
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

        Role role = roleRepository.findByName(roleName.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Set.of(role));
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        emailService.sendRegistrationEmail(savedUser.getEmail(), savedUser.getFirstName(), savedUser.getLastName(),
                role.getName());
        return savedUser;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getPatients() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("PATIENT")))
                .toList();
    }

    public User searchPatientById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRoles().stream().noneMatch(r -> r.getName().equals("PATIENT"))) {
            throw new RuntimeException("User is not a patient");
        }
        return user;
    }

    public User searchPatientByMobile(String mobileNumber) {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRoles().stream().noneMatch(r -> r.getName().equals("PATIENT"))) {
            throw new RuntimeException("User is not a patient");
        }
        return user;
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // --- Appointment Management ---
    public Map<String, Long> getAppointmentStats() {
        Map<String, Long> stats = new HashMap<>();

        LocalDate today = LocalDate.now();

        LocalDateTime startOfDay = today.atStartOfDay(); // 2026-03-22T00:00
        LocalDateTime endOfDay = today.atTime(23, 59, 59); // 2026-03-22T23:59:59

        stats.put("total", appointmentRepository.count());

        stats.put("today",
                appointmentRepository.countByAppointmentDateBetween(startOfDay, endOfDay));

        stats.put("completed",
                appointmentRepository.countByStatus("COMPLETED"));

        return stats;
    }

    private AppointmentResponse mapToResponse(Appointment a, Map<Long, DoctorDetails> doctorMap) {

        DoctorDetails details = doctorMap.get(a.getDoctor().getId());

        String specialization = details != null
                ? details.getSpecialization()
                : "N/A";

        String patientName = a.getIsGuest()
                ? a.getGuestFirstName() + " " + a.getGuestLastName()
                : a.getPatient().getFirstName() + " " + a.getPatient().getLastName();

        String mobile = a.getIsGuest()
                ? a.getGuestMobile()
                : a.getPatient().getMobileNumber();

        return new AppointmentResponse(
                a.getId(),
                a.getAppointmentDate(),
                a.getStatus(),
                a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName(),
                specialization,
                patientName,
                mobile,
                a.getDoctor().getId());
    }

    public List<AppointmentResponse> getAllAppointments() {

        Map<Long, DoctorDetails> doctorMap = doctorDetailsRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getDoctor().getId(),
                        d -> d));

        return appointmentRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentDate).reversed()) // 🔥 latest first
                .map(a -> mapToResponse(a, doctorMap))
                .toList();
    }

    public List<AppointmentResponse> getAppointmentsByDateRange(
            LocalDateTime start,
            LocalDateTime end) {

        Map<Long, DoctorDetails> doctorMap = doctorDetailsRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getDoctor().getId(),
                        d -> d));

        return appointmentRepository.findByAppointmentDateBetween(start, end)
                .stream()
                .sorted(Comparator.comparing(Appointment::getAppointmentDate))
                .map(a -> mapToResponse(a, doctorMap))
                .toList();
    }

    public List<AppointmentResponse> getTodayAppointments() {

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);

        return getAppointmentsByDateRange(startOfDay, endOfDay);
    }

    public List<AppointmentResponse> getFutureAppointments() {

        return getAppointmentsByDateRange(
                LocalDateTime.now(),
                LocalDateTime.now().plusYears(100));
    }

    public List<AppointmentResponse> getPastAppointments() {

        return getAppointmentsByDateRange(
                LocalDateTime.now().minusYears(100),
                LocalDateTime.now());
    }

    public void deleteAppointment(Long appointmentId) {
        appointmentRepository.deleteById(appointmentId);
    }

    public void scheduleGuestAppointment(GuestAppointmentRequest request) {

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // ✅ Prevent double booking (IMPORTANT)
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatusIn(
                request.getDoctorId(),
                request.getAppointmentDate(),
                List.of("SCHEDULED", "CONFIRMED"))) {

            throw new RuntimeException("Slot already booked");
        }

        Appointment appointment = new Appointment();

        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setStatus("SCHEDULED");

        appointment.setIsGuest(true);
        appointment.setGuestFirstName(request.getFirstName());
        appointment.setGuestLastName(request.getLastName());
        appointment.setGuestEmail(request.getEmail());
        appointment.setGuestMobile(request.getMobileNumber());

        Appointment saved = appointmentRepository.save(appointment);

        billingService.createBill(saved);
    }
    // Doctor

    public void saveAvailability(DoctorAvailabilityRequest request) {

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        DoctorAvailability availability = availabilityRepository
                .findByDoctorAndDayOfWeek(doctor, request.getDayOfWeek())
                .orElse(new DoctorAvailability());

        availability.setDoctor(doctor);
        availability.setDayOfWeek(request.getDayOfWeek());
        availability.setStartTime(LocalTime.parse(request.getStartTime()));
        availability.setEndTime(LocalTime.parse(request.getEndTime()));

        availability.setBreakStart(
                request.getBreakStart() != null && !request.getBreakStart().isEmpty()
                        ? LocalTime.parse(request.getBreakStart())
                        : null);

        availability.setBreakEnd(
                request.getBreakEnd() != null && !request.getBreakEnd().isEmpty()
                        ? LocalTime.parse(request.getBreakEnd())
                        : null);

        availability.setSlotDuration(request.getSlotDuration());

        availabilityRepository.save(availability);
    }

    public List<Map<String, Object>> getAvailableSlots(Long doctorId, String dateStr) {

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        LocalDate date = LocalDate.parse(dateStr);
        String day = date.getDayOfWeek().toString();

        DoctorAvailability availability = availabilityRepository
                .findByDoctorAndDayOfWeek(doctor, day)
                .orElseThrow(() -> new RuntimeException("No availability"));

        List<LocalDateTime> slots = new ArrayList<>();

        LocalTime current = availability.getStartTime();

        while (!current.plusMinutes(availability.getSlotDuration())
                .isAfter(availability.getEndTime())) {

            if (availability.getBreakStart() != null &&
                    availability.getBreakEnd() != null &&
                    !current.isBefore(availability.getBreakStart()) &&
                    current.isBefore(availability.getBreakEnd())) {

                current = availability.getBreakEnd();
                continue;
            }

            slots.add(LocalDateTime.of(date, current));
            current = current.plusMinutes(availability.getSlotDuration());
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Appointment> booked = appointmentRepository
                .findByDoctorIdAndAppointmentDateBetweenAndStatus(
                        doctorId, start, end, "SCHEDULED");

        return slots.stream()
                .map(slot -> {
                    boolean isBooked = booked.stream()
                            .anyMatch(b -> b.getAppointmentDate().equals(slot));

                    Map<String, Object> map = new HashMap<>();
                    map.put("time", slot);
                    map.put("available", !isBooked);
                    return map;
                })
                .toList();
    }

    public List<DoctorAvailability> getAvailabilityByDoctor(Long doctorId) {
        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        return availabilityRepository.findAll()
                .stream()
                .filter(a -> a.getDoctor().getId().equals(doctorId))
                .toList();
    }

    public void deleteAvailability(Long id) {
        availabilityRepository.deleteById(id);
    }

    private String generateRandomPassword() {
        return java.util.UUID.randomUUID().toString().substring(0, 8) + "@A1";
    }

    private DoctorResponse mapToDoctorResponse(User user, DoctorDetails details) {
        DoctorResponse dto = new DoctorResponse();

        dto.setUserId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setMobileNumber(user.getMobileNumber());

        dto.setEducation(details.getEducation());
        dto.setSpecialization(details.getSpecialization());
        dto.setYearsOfExperience(details.getYearsOfExperience());
        dto.setPastExperience(details.getPastExperience());
        dto.setConsultationFee(details.getConsultationFee());

        return dto;
    }

    public DoctorResponse registerDoctor(DoctorRegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.findByMobileNumber(request.getMobileNumber()).isPresent()) {
            throw new RuntimeException("Mobile number already exists");
        }

        String tempPassword = generateRandomPassword();

        // Create User
        User doctor = new User();
        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctor.setMobileNumber(request.getMobileNumber());
        doctor.setPassword(passwordEncoder.encode(tempPassword));
        doctor.setEnabled(true);

        Role role = roleRepository.findByName("DOCTOR")
                .orElseThrow(() -> new RuntimeException("DOCTOR role not found"));

        doctor.setRoles(Set.of(role));

        User savedDoctor = userRepository.save(doctor);

        // Create DoctorDetails
        DoctorDetails details = new DoctorDetails();
        details.setDoctor(savedDoctor);
        details.setEducation(request.getEducation());
        details.setSpecialization(request.getSpecialization());
        details.setYearsOfExperience(request.getYearsOfExperience());
        details.setPastExperience(request.getPastExperience());

        doctorDetailsRepository.save(details);

        // Send email with TEMP password
        emailService.sendDoctorRegistrationEmail(
                savedDoctor.getEmail(),
                savedDoctor.getFirstName(),
                tempPassword // plain password
        );

        return mapToDoctorResponse(savedDoctor, details);
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorDetailsRepository.findAll().stream()
                .map(details -> mapToDoctorResponse(details.getDoctor(), details))
                .toList();
    }

    public DoctorResponse getDoctorById(Long id) {

        DoctorDetails details = doctorDetailsRepository
                .findByDoctor_Id(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (details == null) {
            throw new RuntimeException("Doctor not found");
        }

        return mapToDoctorResponse(details.getDoctor(), details);
    }

    public DoctorResponse updateDoctor(Long id, DoctorRegisterRequest request) {

        DoctorDetails details = doctorDetailsRepository
                .findByDoctor_Id(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (details == null) {
            throw new RuntimeException("Doctor not found");
        }

        User doctor = details.getDoctor();

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setMobileNumber(request.getMobileNumber());

        details.setEducation(request.getEducation());
        details.setSpecialization(request.getSpecialization());
        details.setYearsOfExperience(request.getYearsOfExperience());
        details.setPastExperience(request.getPastExperience());

        userRepository.save(doctor);
        doctorDetailsRepository.save(details);

        return mapToDoctorResponse(doctor, details);
    }

    public void deleteDoctor(Long id) {

        DoctorDetails details = doctorDetailsRepository
                .findByDoctor_Id(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        if (details == null) {
            throw new RuntimeException("Doctor not found");
        }

        User doctor = details.getDoctor();

        doctorDetailsRepository.delete(details);
        userRepository.delete(doctor);
    }

    public PatientFullDetailsDto getPatientFullDetails(Long patientId) {

        User patient = userRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (patient.getRoles().stream().noneMatch(r -> r.getName().equals("PATIENT"))) {
            throw new RuntimeException("User is not a patient");
        }

        PatientFullDetailsDto dto = new PatientFullDetailsDto();
        dto.setPatientId(patient.getId());
        dto.setName(patient.getFirstName() + " " + patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setMobile(patient.getMobileNumber());

        // ✅ Load all doctor details once
        Map<Long, DoctorDetails> doctorMap = doctorDetailsRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getDoctor().getId(),
                        d -> d));

        // ================= APPOINTMENTS =================
        List<AppointmentResponse> appointments = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDesc(patientId)
                .stream()
                .map(a -> {

                    String patientName = a.getIsGuest()
                            ? a.getGuestFirstName() + " " + a.getGuestLastName()
                            : a.getPatient().getFirstName() + " " + a.getPatient().getLastName();

                    String mobile = a.getIsGuest()
                            ? a.getGuestMobile()
                            : a.getPatient().getMobileNumber();

                    DoctorDetails details = doctorMap.get(a.getDoctor().getId());

                    String specialization = details != null
                            ? details.getSpecialization()
                            : "N/A";

                    return new AppointmentResponse(
                            a.getId(),
                            a.getAppointmentDate(),
                            a.getStatus(),
                            a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName(),
                            specialization,
                            patientName,
                            mobile,
                            a.getDoctor().getId());
                })
                .toList();

        dto.setAppointments(appointments);

        // ================= BILLS =================
        List<BillResponseDto> bills = billingService.getPatientBills(patientId);
        dto.setBills(bills);

        return dto;
    }
}
