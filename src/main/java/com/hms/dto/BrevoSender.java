package com.hms.dto;

import lombok.Data;

@Data
public class BrevoSender {
    private String name;
    private String email;

    public BrevoSender(String name, String email) {
        this.name = name;
        this.email = email;
    }
}