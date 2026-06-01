package com.hms.dto;

import lombok.Data;

import java.util.List;

@Data
public class BrevoEmailRequest {

    private BrevoSender sender;
    private List<BrevoRecipient> to;
    private String subject;
    private String htmlContent;
}