package com.hms.repository;

import com.hms.dto.AppointmentResponse;
import com.hms.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // =========================
    // PATIENT
    // =========================

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatientIdOrderByAppointmentDateDesc(Long patientId);

    List<Appointment> findByPatientIdAndStatus(Long patientId, String status);

    // =========================
    // DOCTOR
    // =========================

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndStatus(Long doctorId, String status);

    List<Appointment> findByDoctorIdAndAppointmentDateBetween(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end);

    boolean existsByDoctorIdAndAppointmentDate(
            Long doctorId,
            LocalDateTime appointmentDate);

    // =========================
    // PATIENT FULL DETAILS
    // =========================

    @Query("""
                SELECT a
                FROM Appointment a
                JOIN FETCH a.doctor
                JOIN FETCH a.patient
                WHERE a.patient.id = :patientId
                ORDER BY a.appointmentDate DESC
            """)
    List<Appointment> findFullByPatientId(
            @Param("patientId") Long patientId);

    @Query("""
                SELECT new com.hms.dto.AppointmentResponse(
                    a.id,
                    a.appointmentDate,
                    a.status,
                    CONCAT(d.firstName, ' ', d.lastName),
                    COALESCE(dd.specialization, 'N/A'),
                    CASE
                        WHEN a.isGuest = true
                        THEN CONCAT(a.guestFirstName, ' ', a.guestLastName)
                        ELSE CONCAT(p.firstName, ' ', p.lastName)
                    END,
                    CASE
                        WHEN a.isGuest = true
                        THEN a.guestMobile
                        ELSE p.mobileNumber
                    END,
                    d.id
                )
                FROM Appointment a
                JOIN a.doctor d
                LEFT JOIN a.patient p
                LEFT JOIN DoctorDetails dd ON dd.doctor.id = d.id
                ORDER BY a.appointmentDate DESC
            """)
    List<AppointmentResponse> findAllForAdmin();

    @Query("""
                SELECT new com.hms.dto.AppointmentResponse(
                    a.id,
                    a.appointmentDate,
                    a.status,
                    CONCAT(d.firstName, ' ', d.lastName),
                    COALESCE(dd.specialization, 'N/A'),
                    CASE
                        WHEN a.isGuest = true
                        THEN CONCAT(a.guestFirstName, ' ', a.guestLastName)
                        ELSE CONCAT(p.firstName, ' ', p.lastName)
                    END,
                    CASE
                        WHEN a.isGuest = true
                        THEN a.guestMobile
                        ELSE p.mobileNumber
                    END,
                    d.id
                )
                FROM Appointment a
                JOIN a.doctor d
                LEFT JOIN a.patient p
                LEFT JOIN DoctorDetails dd ON dd.doctor.id = d.id
                WHERE a.appointmentDate >= :start
                  AND a.appointmentDate < :end
                ORDER BY a.appointmentDate ASC
            """)
    List<AppointmentResponse> findForAdminByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // =========================
    // EXISTING ADMIN / OTHER
    // =========================

    List<Appointment> findByAppointmentDateBetween(
            LocalDateTime start,
            LocalDateTime end);

    List<Appointment> findByDoctorIdAndAppointmentDateBetweenAndStatus(
            Long doctorId,
            LocalDateTime start,
            LocalDateTime end,
            String status);

    @Query("""
                SELECT a
                FROM Appointment a
                WHERE a.doctor.id = :doctorId
                  AND a.status = :status
                  AND a.appointmentDate >= :start
                  AND a.appointmentDate < :end
            """)
    List<Appointment> findBookedSlots(
            @Param("doctorId") Long doctorId,
            @Param("status") String status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("""
                SELECT a
                FROM Appointment a
                LEFT JOIN FETCH a.patient
                JOIN FETCH a.doctor
                WHERE a.doctor.id = :doctorId
                ORDER BY a.appointmentDate DESC
            """)
    List<Appointment> findDoctorAppointmentsWithDetails(
            @Param("doctorId") Long doctorId);

    // =========================
    // STATS
    // =========================

    long count();

    long countByStatus(String status);

    long countByAppointmentDateBetween(
            LocalDateTime start,
            LocalDateTime end);

    long countByAppointmentDateBetweenAndStatus(
            LocalDateTime start,
            LocalDateTime end,
            String status);

    long countByDoctorId(Long doctorId);

    long countByDoctorIdAndStatus(
            Long doctorId,
            String status);

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

    @Query("""
                SELECT new com.hms.dto.AppointmentResponse(
                    a.id,
                    a.appointmentDate,
                    a.status,
                    CONCAT(d.firstName, ' ', d.lastName),
                    COALESCE(dd.specialization, 'N/A'),
                    CONCAT(p.firstName, ' ', p.lastName),
                    p.mobileNumber,
                    d.id
                )
                FROM Appointment a
                JOIN a.doctor d
                LEFT JOIN a.patient p
                LEFT JOIN DoctorDetails dd ON dd.doctor.id = d.id
                WHERE p.id = :patientId
                ORDER BY a.appointmentDate DESC
            """)
    List<AppointmentResponse> findPatientAppointmentResponses(
            @Param("patientId") Long patientId);

}