package com.hms.repository;

import com.hms.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

        // ✅ Patient
        List<Appointment> findByPatientId(Long patientId);

        List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);

        List<Appointment> findByPatientIdAndStatus(Long patientId, String status);

        // ✅ Doctor
        List<Appointment> findByDoctorId(Long doctorId);

        List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);

        // ✅ FIX: fetch only same-day bookings (IMPORTANT)
        List<Appointment> findByDoctorIdAndAppointmentDateBetween(
                        Long doctorId,
                        LocalDateTime start,
                        LocalDateTime end);

        // ✅ FIX: prevent double booking (CRITICAL)
        boolean existsByDoctorIdAndAppointmentDate(Long doctorId, LocalDateTime appointmentDate);

        @Query("""
                            SELECT a FROM Appointment a
                            JOIN FETCH a.doctor d
                            JOIN FETCH a.patient p
                            WHERE p.id = :patientId
                            ORDER BY a.appointmentDate DESC
                        """)
        List<Appointment> findFullByPatientId(Long patientId);

        // Optional (admin usage)
        List<Appointment> findByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

        List<Appointment> findByDoctorIdAndAppointmentDateBetweenAndStatus(
                        Long doctorId,
                        LocalDateTime start,
                        LocalDateTime end,
                        String status);

        @Query("""
                            SELECT a FROM Appointment a
                            WHERE a.doctor.id = :doctorId
                            AND a.status = :status
                            AND a.appointmentDate >= :start
                            AND a.appointmentDate < :end
                        """)
        List<Appointment> findBookedSlots(
                        Long doctorId,
                        String status,
                        LocalDateTime start,
                        LocalDateTime end);

        @Query("""
                            SELECT a FROM Appointment a
                            LEFT JOIN FETCH a.patient
                            JOIN FETCH a.doctor
                            WHERE a.doctor.id = :doctorId
                            ORDER BY a.appointmentDate DESC
                        """)
        List<Appointment> findDoctorAppointmentsWithDetails(Long doctorId);

        // stats
        long count();

        long countByStatus(String status);

        long countByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

        long countByAppointmentDateBetweenAndStatus(
                        LocalDateTime start,
                        LocalDateTime end,
                        String status);

        long countByDoctorId(Long doctorId);

        long countByDoctorIdAndStatus(Long doctorId, String status);

        long countByDoctorIdAndAppointmentDateBetween(
                        Long doctorId,
                        LocalDateTime start,
                        LocalDateTime end);

        long countByDoctorIdAndStatusAndAppointmentDateBetween(
                        Long doctorId,
                        String status,
                        LocalDateTime start,
                        LocalDateTime end);

        boolean existsByDoctorIdAndAppointmentDateAndStatusIn(
                        Long doctorId,
                        LocalDateTime appointmentDate,
                        List<String> statuses);
}