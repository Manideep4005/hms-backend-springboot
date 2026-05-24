package com.hms.repository;

import com.hms.entity.DoctorDetails;
import com.hms.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorDetailsRepository extends JpaRepository<DoctorDetails, Long> {

    Optional<DoctorDetails> findByDoctor_Id(Long userId);

    Optional<DoctorDetails> findByDoctor(User doctor);

}