package com.hms.service;

import com.hms.dto.BrevoEmailRequest;
import com.hms.dto.BrevoRecipient;
import com.hms.dto.BrevoSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrevoEmailService {

        private final RestClient restClient = RestClient.builder()
                        .baseUrl("https://api.brevo.com/v3/smtp/email")
                        .build();

        @Value("${brevo.api-key}")
        private String apiKey;

        @Value("${brevo.sender-email}")
        private String senderEmail;

        public void sendEmail(
                        String toEmail,
                        String subject,
                        String htmlContent) {

                BrevoEmailRequest request = new BrevoEmailRequest();

                request.setSender(
                                new BrevoSender(
                                                "Hospital Management System",
                                                senderEmail));

                request.setTo(
                                List.of(new BrevoRecipient(toEmail)));

                request.setSubject(subject);
                request.setHtmlContent(htmlContent);

                try {
                        restClient.post()
                                        .header("api-key", apiKey)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .body(request)
                                        .retrieve()
                                        .toBodilessEntity();

                        System.out.println("Email sent successfully");

                } catch (Exception e) {
                        System.err.println("Failed to send email");
                        e.printStackTrace();
                }
        }
}