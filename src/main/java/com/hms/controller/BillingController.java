package com.hms.controller;

import com.hms.entity.Bill;
import com.hms.service.BillingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/{appointmentId}")
    public Bill getBill(@PathVariable Long appointmentId) {
        return billingService.getBillByAppointment(appointmentId);
    }

    @PutMapping("/pay/{billId}")
    public String markPaid(@PathVariable Long billId) {
        billingService.markAsPaid(billId);
        return "Bill marked as PAID";
    }
}
