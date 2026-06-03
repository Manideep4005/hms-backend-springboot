package com.hms.service;

import com.hms.dto.AppointmentRequestDto;
import com.hms.dto.AppointmentResponse;
import com.hms.dto.ChangePasswordRequest;
import com.hms.dto.UserProfileDto;
import com.hms.entity.Appointment;
import com.hms.entity.Bill;
import com.hms.entity.DoctorAvailability;
import com.hms.entity.DoctorDetails;
import com.hms.entity.User;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorAvailabilityRepository;
import com.hms.repository.DoctorDetailsRepository;
import com.hms.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final DoctorDetailsRepository doctorDetailsRepository;
    private final DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private BillingService billingService;

    public PatientService(UserRepository userRepository, AppointmentRepository appointmentRepository,
            PasswordEncoder passwordEncoder, EmailService emailService,
            DoctorAvailabilityRepository availabilityRepository, DoctorDetailsRepository doctorDetailsRepository) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.availabilityRepository = availabilityRepository;
        this.doctorDetailsRepository = doctorDetailsRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    public UserProfileDto getProfile(String email) {
        User user = getUserByEmail(email);
        UserProfileDto dto = new UserProfileDto();
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setMobileNumber(user.getMobileNumber());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public void updateProfile(String email, UserProfileDto dto) {
        User user = getUserByEmail(email);
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMobileNumber(dto.getMobileNumber());
        userRepository.save(user);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Incorrect old password");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        emailService.sendPasswordChangeEmail(user.getEmail(), user.getFirstName());
    }

    public List<User> getAvailableDoctors() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.getName().equals("DOCTOR")))
                .toList();
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

    public void bookAppointment(String email, AppointmentRequestDto request) {

        User patient = getUserByEmail(email);

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // ✅ validate doctor
        boolean isDoctor = doctor.getRoles().stream()
                .anyMatch(r -> r.getName().equals("DOCTOR"));

        if (!isDoctor) {
            throw new RuntimeException("Selected user is not a doctor");
        }

        // ✅ CRITICAL: prevent double booking
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatusIn(
                request.getDoctorId(),
                request.getAppointmentDate(),
                List.of("SCHEDULED", "CONFIRMED"))) {

            throw new RuntimeException("Slot already booked");
        }
        // ✅ Save
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentType("ONLINE");
        appointment.setStatus("SCHEDULED");
        appointment.setIsGuest(false);

        Appointment saved = appointmentRepository.save(appointment);

        Bill bill = billingService.createBill(saved);

        // ✅ Email
        emailService.sendAppointmentBookedEmail(
                patient.getEmail(),
                patient.getFirstName(),
                doctor.getFirstName() + " " + doctor.getLastName(),
                request.getAppointmentDate(),
                bill.getTotalAmount(),
                saved.getId());
    }

    public void cancelAppointment(Long appointmentId, String email) {
        User patient = getUserByEmail(email);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Unauthorized to cancel this appointment");
        }
        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);

        billingService.cancelBill(appointmentId);
    }

    public void editAppointment(Long appointmentId, AppointmentRequestDto request, String email) {

        User patient = getUserByEmail(email);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getPatient().getId().equals(patient.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        if (!appointment.getStatus().equals("SCHEDULED")) {
            throw new RuntimeException("Only scheduled appointments can be edited");
        }

        // ✅ prevent double booking
        boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentDate(
                request.getDoctorId(),
                request.getAppointmentDate());

        if (exists && !appointment.getAppointmentDate().equals(request.getAppointmentDate())) {
            throw new RuntimeException("Slot already booked");
        }

        User doctor = userRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(request.getAppointmentDate());

        appointmentRepository.save(appointment);
    }

    public List<AppointmentResponse> getAppointmentHistory(String email, String filter) {

        User patient = getUserByEmail(email);

        // ✅ Load doctor details map (SAME AS ADMIN)
        Map<Long, DoctorDetails> doctorMap = doctorDetailsRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        d -> d.getDoctor().getId(),
                        d -> d));

        List<Appointment> appointments;

        if (filter == null || filter.isEmpty() || filter.equalsIgnoreCase("ALL")) {
            appointments = appointmentRepository.findFullByPatientId(patient.getId());
        } else {
            appointments = appointmentRepository.findByPatientIdAndStatus(
                    patient.getId(), filter.toUpperCase());
        }

        return appointments.stream()
                .map(a -> {

                    // ✅ Doctor name
                    String doctorName = a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName();

                    // ✅ FIX: get specialization from map
                    DoctorDetails details = doctorMap.get(a.getDoctor().getId());

                    String specialization = details != null
                            ? details.getSpecialization()
                            : "N/A";

                    // ✅ Patient
                    String patientName = a.getPatient().getFirstName() + " " + a.getPatient().getLastName();

                    return new AppointmentResponse(
                            a.getId(),
                            a.getAppointmentDate(),
                            a.getStatus(),
                            doctorName,
                            specialization,
                            patientName,
                            a.getPatient().getMobileNumber(), a.getDoctor().getId());
                })
                .toList();
    }
}
