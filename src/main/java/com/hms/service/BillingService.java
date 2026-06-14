package com.hms.service;

import com.hms.dto.BillItemDto;
import com.hms.dto.BillResponseDto;
import com.hms.entity.*;
import com.hms.repository.AppointmentRepository;
import com.hms.repository.BillRepository;
import com.hms.repository.DoctorDetailsRepository;
import com.hms.repository.UserRepository;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BillingService {

        private final BillRepository billRepository;
        private final DoctorDetailsRepository doctorDetailsRepository;
        private final AppointmentRepository appointmentRepository;
        private final UserRepository userRepository;

        public BillingService(BillRepository billRepository,
                        DoctorDetailsRepository doctorDetailsRepository,
                        AppointmentRepository appointmentRepository,
                        UserRepository userRepository) {
                this.billRepository = billRepository;
                this.doctorDetailsRepository = doctorDetailsRepository;
                this.appointmentRepository = appointmentRepository;
                this.userRepository = userRepository;
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

        public Bill getBillByAppointment(Long appointmentId) {
                return billRepository.findByAppointmentId(appointmentId)
                                .orElseThrow(() -> new RuntimeException("Bill not found"));
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

                List<Bill> bills = billRepository.findByPatientIdWithItems(patientId);

                return bills.stream().map(bill -> {

                        Appointment appointment = appointmentRepository
                                        .findById(bill.getAppointmentId())
                                        .orElse(null);

                        User doctor = userRepository
                                        .findById(bill.getDoctorId())
                                        .orElse(null);

                        DoctorDetails doctorDetails = doctorDetailsRepository
                                        .findByDoctor_Id(bill.getDoctorId())
                                        .orElse(null);

                        String patientName = null;

                        if (appointment != null) {

                                if (Boolean.TRUE.equals(appointment.getIsGuest())) {

                                        String firstName = appointment.getGuestFirstName() != null
                                                        ? appointment.getGuestFirstName()
                                                        : "";

                                        String lastName = appointment.getGuestLastName() != null
                                                        ? appointment.getGuestLastName()
                                                        : "";

                                        patientName = (firstName + " " + lastName).trim();

                                } else if (appointment.getPatient() != null) {

                                        patientName = appointment.getPatient().getFirstName() + " "
                                                        + appointment.getPatient().getLastName();
                                }
                        }

                        List<BillItemDto> items = bill.getItems().stream()
                                        .map(i -> new BillItemDto(
                                                        i.getItemName(),
                                                        i.getQuantity(),
                                                        i.getPrice(),
                                                        i.getTotal()))
                                        .toList();

                        return new BillResponseDto(
                                        bill.getId(),
                                        bill.getAmount(),
                                        bill.getDiscount(),
                                        bill.getTax(),
                                        bill.getTotalAmount(),
                                        bill.getStatus().name(),
                                        bill.getBillDate(),
                                        items,
                                        bill.getAppointmentId(),
                                        doctor != null
                                                        ? doctor.getFirstName() + " " + doctor.getLastName()
                                                        : null,
                                        doctorDetails != null
                                                        ? doctorDetails.getSpecialization()
                                                        : null,
                                        appointment != null
                                                        ? appointment.getAppointmentDate()
                                                        : null,
                                        patientName);

                }).toList();
        }

        @Transactional(readOnly = true)
        public List<BillResponseDto> getAllBills() {

                List<Bill> bills = billRepository.findAllWithItems();

                return bills.stream().map(bill -> {

                        Appointment appointment = appointmentRepository
                                        .findById(bill.getAppointmentId())
                                        .orElse(null);

                        User doctor = userRepository
                                        .findById(bill.getDoctorId())
                                        .orElse(null);

                        DoctorDetails doctorDetails = doctorDetailsRepository
                                        .findByDoctor_Id(bill.getDoctorId())
                                        .orElse(null);

                        String patientName = null;

                        if (appointment != null) {

                                if (Boolean.TRUE.equals(appointment.getIsGuest())) {

                                        String firstName = appointment.getGuestFirstName() != null
                                                        ? appointment.getGuestFirstName()
                                                        : "";

                                        String lastName = appointment.getGuestLastName() != null
                                                        ? appointment.getGuestLastName()
                                                        : "";

                                        patientName = (firstName + " " + lastName).trim();

                                } else if (appointment.getPatient() != null) {

                                        patientName = appointment.getPatient().getFirstName() + " "
                                                        + appointment.getPatient().getLastName();
                                }
                        }

                        List<BillItemDto> items = bill.getItems().stream()
                                        .map(i -> new BillItemDto(
                                                        i.getItemName(),
                                                        i.getQuantity(),
                                                        i.getPrice(),
                                                        i.getTotal()))
                                        .toList();

                        return new BillResponseDto(
                                        bill.getId(),
                                        bill.getAmount(),
                                        bill.getDiscount(),
                                        bill.getTax(),
                                        bill.getTotalAmount(),
                                        bill.getStatus().name(),
                                        bill.getBillDate(),
                                        items,
                                        bill.getAppointmentId(),
                                        doctor != null
                                                        ? doctor.getFirstName() + " " + doctor.getLastName()
                                                        : null,
                                        doctorDetails != null
                                                        ? doctorDetails.getSpecialization()
                                                        : null,
                                        appointment != null
                                                        ? appointment.getAppointmentDate()
                                                        : null,
                                        patientName);

                }).toList();
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