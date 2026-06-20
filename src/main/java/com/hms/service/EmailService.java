package com.hms.service;

import jakarta.mail.MessagingException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final BrevoEmailService brevoEmailService;
    private final TemplateEngine templateEngine;

    public EmailService(BrevoEmailService brevoEmailService, TemplateEngine templateEngine) {
        this.brevoEmailService = brevoEmailService;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendRegistrationEmail(
            String to,
            String firstName,
            String lastName,
            String roleName) {

        try {
            Context context = new Context();

            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);
            context.setVariable("roleName", roleName);
            context.setVariable("email", to);

            String process = templateEngine.process(
                    "email/registration-template",
                    context);

            sendHtmlMessage(to, "Welcome to HMS", process);

        } catch (Exception e) {
            System.err.println("Failed to send Registration email");
            e.printStackTrace();
        }
    }

    @Async
    public void AdminsendRegistrationEmail(
            String to,
            String firstName,
            String lastName,
            String roleName, String tempPassword) {

        try {
            Context context = new Context();

            context.setVariable("firstName", firstName);
            context.setVariable("lastName", lastName);
            context.setVariable("roleName", roleName);
            context.setVariable("email", to);
            context.setVariable("tempPassword", tempPassword);

            String process = templateEngine.process(
                    "email/admin-register-user-template",
                    context);

            sendHtmlMessage(to, "Your Account has been created", process);

        } catch (Exception e) {
            System.err.println("Failed to send Registration email");
            e.printStackTrace();
        }
    }

    @Async
    public void sendPasswordChangeEmail(String to, String firstName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);

            String process = templateEngine.process("email/password-change-template", context);
            sendHtmlMessage(to, "Password Changed Successfully", process);
        } catch (Exception e) {
            System.err.println("Failed to send Password Change email: ");
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentBookedEmail(
            String to,
            String patientName,
            String doctorName,
            LocalDateTime appointmentDate,
            Double consultationFee,
            Long appointmentId) {

        try {
            Context context = new Context();

            String formattedDate = appointmentDate.format(
                    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));

            String formattedFee = String.format("%.2f", consultationFee);

            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("date", formattedDate);
            context.setVariable("consultationFee", formattedFee);
            context.setVariable("appointmentId", appointmentId);

            String html = templateEngine.process(
                    "email/appointment-booked-template",
                    context);

            sendHtmlMessage(to, "Appointment Confirmed - HMS", html);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendReviewRequiredEmail(String to, String patientName, String doctorName, String reviewPeriod) {
        try {
            Context context = new Context();
            context.setVariable("patientName", patientName);
            context.setVariable("doctorName", doctorName);
            context.setVariable("reviewPeriod", reviewPeriod);

            String process = templateEngine.process("email/review-required-template", context);
            sendHtmlMessage(to, "Medical Review Required", process);
        } catch (Exception e) {
            System.err.println("Failed to send Review Required email: ");
            e.printStackTrace();
        }
    }

    private void sendHtmlMessage(String to, String subject, String htmlBody) throws MessagingException {

        brevoEmailService.sendEmail(to, subject, htmlBody);
    }

    @Async
    public void sendDoctorRegistrationEmail(String to, String firstName, String tempPassword) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("email", to);
            context.setVariable("tempPassword", tempPassword);

            String process = templateEngine.process("email/doctor-registration-template", context);
            sendHtmlMessage(to, "Your HMS Doctor Account Credentials", process);
        } catch (Exception e) {
            System.err.println("Failed to send Doctor Registration email: ");
            e.printStackTrace();
        }
    }

    @Async
    public void sendForgotPasswordOtpEmail(String to, String firstName, String otp) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("otp", otp);

            String process = templateEngine.process("email/forgot-password-otp-template", context);
            sendHtmlMessage(to, "Your HMS Password Reset OTP", process);

        } catch (Exception e) {
            System.err.println("Failed to send Forgot Password OTP email");
            e.printStackTrace();
        }
    }

    @Async
    public void sendPasswordResetSuccessEmail(String to, String firstName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);

            String process = templateEngine.process("email/password-reset-success-template", context);
            sendHtmlMessage(to, "Your HMS Password Has Been Reset", process);

        } catch (Exception e) {
            System.err.println("Failed to send Password Reset Success email: ");
            e.printStackTrace();
        }
    }
}
