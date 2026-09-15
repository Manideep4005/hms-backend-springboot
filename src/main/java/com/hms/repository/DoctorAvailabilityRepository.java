package com.hms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hms.entity.DoctorAvailability;
import com.hms.entity.User;

public interface DoctorAvailabilityRepository
        extends JpaRepository<DoctorAvailability, Long> {

    Optional<DoctorAvailability> findByDoctorAndDayOfWeek(
            User doctor,
            String dayOfWeek);

    @Query("""
                SELECT
                    a.id AS id,
                    a.doctor.id AS doctorId,
                    a.dayOfWeek AS dayOfWeek,
                    a.startTime AS startTime,
                    a.endTime AS endTime,
                    a.breakStart AS breakStart,
                    a.breakEnd AS breakEnd,
                    a.slotDuration AS slotDuration
                FROM DoctorAvailability a
                WHERE a.doctor.id = :doctorId
                ORDER BY a.dayOfWeek, a.startTime
            """)
    List<DoctorAvailabilityProjection> findAvailabilityByDoctor(
            @Param("doctorId") Long doctorId);
}