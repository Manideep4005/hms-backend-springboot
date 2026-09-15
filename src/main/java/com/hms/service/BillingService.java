package com.hms.service;

import com.hms.dto.BillItemDto;
import com.hms.dto.BillResponseDto;
import com.hms.entity.*;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillItemProjection;
import com.hms.repository.BillProjection;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorDetailsRepository;
import com.hms.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class BillingService {

        private final BillRepository billRepository;
        private final DoctorDetailsRepository doctorDetailsRepository;

        public BillingService(BillRepository billRepository,
                        DoctorDetailsRepository doctorDetailsRepository,
                        AppointmentRepository appointmentRepository,
                        UserRepository userRepository) {
                this.billRepository = billRepository;
                this.doctorDetailsRepository = doctorDetailsRepository;
        }

        /* ================= CREATE BILL ================= */

        public Bill createBill(Appointment appointment) {

                Long doctorId = appointment.getDoctor().getId();

                DoctorDetails details = doctorDetailsRepository
                                .findByDoctor_Id(doctorId)
                                .orElseThrow(() -> new RuntimeException("Doctor details not found"));

                Double fee = details.getConsultationFee();

                if (fee == null) {
                        throw new RuntimeException("Consultation fee not configured");
                }

                Bill bill = new Bill();
                bill.setAppointmentId(appointment.getId());

                if (appointment.getPatient() != null) {
                        bill.setPatientId(appointment.getPatient().getId());
                }

                bill.setDoctorId(doctorId);

                // Create Bill Item
                BillItem item = new BillItem();
                item.setItemName("Consultation Fee");
                item.setQuantity(1);
                item.setPrice(fee);
                item.setTotal(fee);
                item.setBill(bill);

                bill.setItems(new ArrayList<>());
                bill.getItems().add(item);

                bill.setAmount(fee);
                bill.setTotalAmount(fee);

                return billRepository.save(bill);
        }

        /* ================= GET BILL ================= */

        @Transactional(readOnly = true)
        public BillResponseDto getBillByAppointment(Long appointmentId) {

                BillProjection bill = billRepository.findBillProjectionByAppointmentId(appointmentId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                List<BillItemProjection> itemProjections = billRepository.findItemsByBillIds(
                                List.of(bill.getId()));

                List<BillItemDto> items = itemProjections.stream()
                                .map(item -> new BillItemDto(
                                                item.getItemName(),
                                                item.getQuantity(),
                                                item.getPrice(),
                                                item.getTotal()))
                                .toList();

                return new BillResponseDto(
                                bill.getId(),
                                bill.getAmount(),
                                bill.getDiscount(),
                                bill.getTax(),
                                bill.getTotalAmount(),
                                bill.getStatus(),
                                bill.getBillDate(),
                                items,
                                bill.getAppointmentId(),
                                bill.getDoctorName(),
                                bill.getSpecialization(),
                                bill.getAppointmentDate(),
                                bill.getPatientName());
        }

        /* ================= MARK PAID ================= */

        public void markAsPaid(Long billId) {
                Bill bill = billRepository.findById(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                bill.setStatus(BillStatus.PAID);
                billRepository.save(bill);
        }

        /* ================= CANCEL BILL ================= */

        public void cancelBill(Long appointmentId) {
                Bill bill = billRepository.findByAppointmentId(appointmentId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                bill.setStatus(BillStatus.CANCELLED);
                billRepository.save(bill);
        }

        @Transactional(readOnly = true)
        public List<BillResponseDto> getPatientBills(Long patientId) {

                List<BillProjection> bills = billRepository.findPatientBillProjections(patientId);

                if (bills.isEmpty()) {
                        return List.of();
                }

                List<Long> billIds = bills.stream()
                                .map(BillProjection::getId)
                                .toList();

                List<BillItemProjection> itemProjections = billRepository.findItemsByBillIds(billIds);

                java.util.Map<Long, List<BillItemDto>> itemsByBill = new java.util.HashMap<>();

                for (BillItemProjection item : itemProjections) {

                        itemsByBill
                                        .computeIfAbsent(
                                                        item.getBillId(),
                                                        id -> new ArrayList<>())
                                        .add(
                                                        new BillItemDto(
                                                                        item.getItemName(),
                                                                        item.getQuantity(),
                                                                        item.getPrice(),
                                                                        item.getTotal()));
                }

                return bills.stream()
                                .map(bill -> {

                                        List<BillItemDto> items = itemsByBill.getOrDefault(
                                                        bill.getId(),
                                                        List.of());

                                        return new BillResponseDto(
                                                        bill.getId(),
                                                        bill.getAmount(),
                                                        bill.getDiscount(),
                                                        bill.getTax(),
                                                        bill.getTotalAmount(),
                                                        bill.getStatus(),
                                                        bill.getBillDate(),
                                                        items,
                                                        bill.getAppointmentId(),
                                                        bill.getDoctorName(),
                                                        bill.getSpecialization(),
                                                        bill.getAppointmentDate(),
                                                        bill.getPatientName());
                                })
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<BillResponseDto> getAllBills() {

                List<BillProjection> bills = billRepository.findAllBillProjections();

                if (bills.isEmpty()) {
                        return List.of();
                }

                List<Long> billIds = bills.stream()
                                .map(BillProjection::getId)
                                .toList();

                List<BillItemProjection> itemProjections = billRepository.findItemsByBillIds(billIds);

                Map<Long, List<BillItemDto>> itemsByBill = new HashMap<>();

                for (BillItemProjection item : itemProjections) {

                        itemsByBill
                                        .computeIfAbsent(
                                                        item.getBillId(),
                                                        id -> new ArrayList<>())
                                        .add(
                                                        new BillItemDto(
                                                                        item.getItemName(),
                                                                        item.getQuantity(),
                                                                        item.getPrice(),
                                                                        item.getTotal()));
                }

                return bills.stream()
                                .map(bill -> {

                                        List<BillItemDto> items = itemsByBill.getOrDefault(
                                                        bill.getId(),
                                                        List.of());

                                        return new BillResponseDto(
                                                        bill.getId(),
                                                        bill.getAmount(),
                                                        bill.getDiscount(),
                                                        bill.getTax(),
                                                        bill.getTotalAmount(),
                                                        bill.getStatus(),
                                                        bill.getBillDate(),
                                                        items,
                                                        bill.getAppointmentId(),
                                                        bill.getDoctorName(),
                                                        bill.getSpecialization(),
                                                        bill.getAppointmentDate(),
                                                        bill.getPatientName());
                                })
                                .toList();
        }

        public void payBillByPatient(Long billId, Long patientId) {

                Bill bill = billRepository.findById(billId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));

                if (!bill.getPatientId().equals(patientId)) {
                        throw new RuntimeException("Unauthorized access");
                }

                if (bill.getStatus() == BillStatus.PAID) {
                        throw new RuntimeException("Bill already paid");
                }

                bill.setStatus(BillStatus.PAID);
                billRepository.save(bill);
        }
}