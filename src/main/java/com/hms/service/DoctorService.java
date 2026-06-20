package com.hms.service;

import com.hms.dto.ChangePasswordRequest;
import com.hms.dto.CompleteAppointmentRequest;
import com.hms.dto.DoctorAppointmentDTO;
import com.hms.dto.DoctorProfileDto;
import com.hms.entity.Appointment;
import com.hms.entity.DoctorDetails;
import com.hms.entity.User;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.DoctorDetailsRepository;
import com.hms.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorDetailsRepository doctorDetailsRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public DoctorService(UserRepository userRepository, DoctorDetailsRepository doctorDetailsRepository,
            AppointmentRepository appointmentRepository, PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.doctorDetailsRepository = doctorDetailsRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    private User getDoctorByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Doctor not found"));
    }

    public DoctorProfileDto getProfile(String email) {
        User doctor = getDoctorByEmail(email);

        DoctorProfileDto dto = new DoctorProfileDto();
        dto.setFirstName(doctor.getFirstName());
        dto.setLastName(doctor.getLastName());
        dto.setMobileNumber(doctor.getMobileNumber());
        dto.setEmail(doctor.getEmail());

        Optional<DoctorDetails> detailsOpt = doctorDetailsRepository.findByDoctor_Id(doctor.getId());

        detailsOpt.ifPresent(details -> {
            dto.setEducation(details.getEducation());
            dto.setSpecialization(details.getSpecialization());
            dto.setYearsOfExperience(details.getYearsOfExperience());
            dto.setPastExperience(details.getPastExperience());
        });

        return dto;
    }

    public void updateProfile(String email, DoctorProfileDto dto) {

        User doctor = getDoctorByEmail(email);

        doctor.setFirstName(dto.getFirstName());
        doctor.setLastName(dto.getLastName());
        doctor.setMobileNumber(dto.getMobileNumber());
        userRepository.save(doctor);

        DoctorDetails details = doctorDetailsRepository
                .findByDoctor_Id(doctor.getId())
                .orElse(new DoctorDetails());

        details.setDoctor(doctor);
        details.setEducation(dto.getEducation());
        details.setSpecialization(dto.getSpecialization());
        details.setYearsOfExperience(dto.getYearsOfExperience());
        details.setPastExperience(dto.getPastExperience());

        doctorDetailsRepository.save(details);
    }

    public void changePassword(String email, ChangePasswordRequest request) {

        User doctor = getDoctorByEmail(email);

        if (!doctor.isPasswordChangeRequired()) {

            if (!passwordEncoder.matches(
                    request.getOldPassword(),
                    doctor.getPassword())) {

                throw new RuntimeException(
                        "Incorrect old password");
            }
        }

        doctor.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()));

        doctor.setPasswordChangeRequired(false);

        userRepository.save(doctor);

        emailService.sendPasswordChangeEmail(
                doctor.getEmail(),
                doctor.getFirstName());
    }

    public Map<String, Long> getDoctorAppointmentStats(String email) {

        User doctor = getDoctorByEmail(email);

        Long doctorId = doctor.getId();

        Map<String, Long> stats = new HashMap<>();

        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        stats.put("total", appointmentRepository.countByDoctorId(doctorId));
        stats.put("today", appointmentRepository.countByDoctorIdAndAppointmentDateBetween(doctorId, start, end));
        stats.put("completed", appointmentRepository.countByDoctorIdAndStatus(doctorId, "COMPLETED"));

        stats.put("todayCompleted",
                appointmentRepository.countByDoctorIdAndStatusAndAppointmentDateBetween(
                        doctorId, "COMPLETED", start, end));

        stats.put("todayPending",
                appointmentRepository.countByDoctorIdAndStatusAndAppointmentDateBetween(
                        doctorId, "PENDING", start, end));

        stats.put("todayCancelled",
                appointmentRepository.countByDoctorIdAndStatusAndAppointmentDateBetween(
                        doctorId, "CANCELLED", start, end));

        return stats;
    }

    public List<DoctorAppointmentDTO> getScheduledAppointments(String email) {
        User doctor = getDoctorByEmail(email);

        return appointmentRepository
                .findDoctorAppointmentsWithDetails(doctor.getId())
                .stream()
                .map(a -> {
                    DoctorAppointmentDTO dto = new DoctorAppointmentDTO();

                    dto.setId(a.getId());
                    dto.setStatus(a.getStatus());
                    dto.setAppointmentDate(a.getAppointmentDate().toString());
                    dto.setIsGuest(a.getIsGuest());

                    if (Boolean.TRUE.equals(a.getIsGuest())) {
                        dto.setPatientName(a.getGuestFirstName() + " " + a.getGuestLastName());
                    } else {
                        dto.setPatientName(
                                a.getPatient().getFirstName() + " " + a.getPatient().getLastName());
                    }

                    dto.setDoctorName(
                            a.getDoctor().getFirstName() + " " + a.getDoctor().getLastName());

                    dto.setRemarks(a.getRemarks());
                    dto.setPrescription(a.getPrescription());
                    dto.setNeedsReview(a.getNeedsReview());
                    dto.setReviewTimeperiod(a.getReviewTimeperiod());

                    return dto;
                })
                .toList();
    }

    @Transactional
    public void markAppointmentAsCompleted(String email, Long appointmentId, CompleteAppointmentRequest request) {
        User doctor = getDoctorByEmail(email);
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        if (!appointment.getDoctor().getId().equals(doctor.getId())) {
            throw new RuntimeException("Unauthorized: Appointment not assigned to this doctor");
        }

        appointment.setStatus("COMPLETED");
        appointment.setRemarks(request.getRemarks());
        appointment.setPrescription(request.getPrescription());

        if (request.getNeedsReview() != null) {
            appointment.setNeedsReview(request.getNeedsReview());
        }
        appointment.setReviewTimeperiod(request.getReviewTimeperiod());

        appointmentRepository.save(appointment);

        if (Boolean.TRUE.equals(request.getNeedsReview())) {
            emailService.sendReviewRequiredEmail(
                    appointment.getPatient().getEmail(),
                    appointment.getPatient().getFirstName(),
                    doctor.getFirstName() + " " + doctor.getLastName(),
                    request.getReviewTimeperiod());
        }
    }
}
