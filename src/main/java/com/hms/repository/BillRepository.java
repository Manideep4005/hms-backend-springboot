package com.hms.repository;

import com.hms.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.*;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByAppointmentId(Long appointmentId);

    List<Bill> findByPatientId(Long patientId);

    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.items ORDER BY b.billDate DESC")
    List<Bill> findAllWithItems();

    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.items WHERE b.patientId = :patientId ORDER BY b.billDate DESC")
    List<Bill> findByPatientIdWithItems(Long patientId);
}