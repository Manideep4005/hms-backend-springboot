package com.hms.repository;

import com.hms.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByAppointmentId(Long appointmentId);

    List<Bill> findByPatientId(Long patientId);

    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.items ORDER BY b.billDate DESC")
    List<Bill> findAllWithItems();

    @Query("SELECT b FROM Bill b LEFT JOIN FETCH b.items WHERE b.patientId = :patientId ORDER BY b.billDate DESC")
    List<Bill> findByPatientIdWithItems(Long patientId);

    @Query("""
                SELECT
                    b.id AS id,
                    b.amount AS amount,
                    b.discount AS discount,
                    b.tax AS tax,
                    b.totalAmount AS totalAmount,
                    CAST(b.status AS string) AS status,
                    b.billDate AS billDate,
                    b.appointmentId AS appointmentId,

                    CONCAT(d.firstName, ' ', d.lastName) AS doctorName,

                    COALESCE(dd.specialization, 'N/A') AS specialization,

                    a.appointmentDate AS appointmentDate,

                    CASE
                        WHEN a.isGuest = true
                        THEN CONCAT(a.guestFirstName, ' ', a.guestLastName)
                        ELSE CONCAT(p.firstName, ' ', p.lastName)
                    END AS patientName

                FROM Bill b
                LEFT JOIN Appointment a
                    ON a.id = b.appointmentId
                LEFT JOIN User d
                    ON d.id = b.doctorId
                LEFT JOIN User p
                    ON p.id = b.patientId
                LEFT JOIN DoctorDetails dd
                    ON dd.doctor.id = b.doctorId

                WHERE b.patientId = :patientId

                ORDER BY b.billDate DESC
            """)
    List<BillProjection> findPatientBillProjections(Long patientId);

    @Query("""
                SELECT
                    bi.bill.id AS billId,
                    bi.itemName AS itemName,
                    bi.quantity AS quantity,
                    bi.price AS price,
                    bi.total AS total

                FROM BillItem bi

                WHERE bi.bill.id IN :billIds

                ORDER BY bi.bill.id, bi.id
            """)
    List<BillItemProjection> findItemsByBillIds(
            @Param("billIds") List<Long> billIds);

    @Query("""
                SELECT
                    b.id AS id,
                    b.amount AS amount,
                    b.discount AS discount,
                    b.tax AS tax,
                    b.totalAmount AS totalAmount,
                    CAST(b.status AS string) AS status,
                    b.billDate AS billDate,
                    b.appointmentId AS appointmentId,

                    CONCAT(d.firstName, ' ', d.lastName) AS doctorName,

                    COALESCE(dd.specialization, 'N/A') AS specialization,

                    a.appointmentDate AS appointmentDate,

                    CASE
                        WHEN a.isGuest = true
                        THEN CONCAT(a.guestFirstName, ' ', a.guestLastName)
                        ELSE CONCAT(p.firstName, ' ', p.lastName)
                    END AS patientName

                FROM Bill b

                LEFT JOIN Appointment a
                    ON a.id = b.appointmentId

                LEFT JOIN User d
                    ON d.id = b.doctorId

                LEFT JOIN User p
                    ON p.id = b.patientId

                LEFT JOIN DoctorDetails dd
                    ON dd.doctor.id = b.doctorId

                ORDER BY b.billDate DESC
            """)
    List<BillProjection> findAllBillProjections();

    @Query("""
                SELECT
                    b.id AS id,
                    b.amount AS amount,
                    b.discount AS discount,
                    b.tax AS tax,
                    b.totalAmount AS totalAmount,
                    CAST(b.status AS string) AS status,
                    b.billDate AS billDate,
                    b.appointmentId AS appointmentId,

                    CONCAT(d.firstName, ' ', d.lastName) AS doctorName,

                    COALESCE(dd.specialization, 'N/A') AS specialization,

                    a.appointmentDate AS appointmentDate,

                    CASE
                        WHEN a.isGuest = true
                        THEN CONCAT(a.guestFirstName, ' ', a.guestLastName)
                        ELSE CONCAT(p.firstName, ' ', p.lastName)
                    END AS patientName

                FROM Bill b

                LEFT JOIN Appointment a
                    ON a.id = b.appointmentId

                LEFT JOIN User d
                    ON d.id = b.doctorId

                LEFT JOIN User p
                    ON p.id = b.patientId

                LEFT JOIN DoctorDetails dd
                    ON dd.doctor.id = b.doctorId

                WHERE b.appointmentId = :appointmentId
            """)
    Optional<BillProjection> findBillProjectionByAppointmentId(
            @Param("appointmentId") Long appointmentId);

}