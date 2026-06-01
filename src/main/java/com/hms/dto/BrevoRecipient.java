package com.hms.dto;

import lombok.Data;

@Data
public class BrevoRecipient {
    private String email;

    public BrevoRecipient(String email) {
        this.email = email;
    }
}