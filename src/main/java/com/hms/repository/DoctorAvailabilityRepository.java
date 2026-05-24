package com.hms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hms.entity.DoctorAvailability;
import com.hms.entity.User;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

    Optional<DoctorAvailability> findByDoctorAndDayOfWeek(User doctor, String dayOfWeek);
}