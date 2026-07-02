package com.greenharvest.auth.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender; // <-- Change this line here!
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@greenharvest.com");
        message.setTo(toEmail);
        message.setSubject("GreenHarvest Secure Portal Access Token");
        message.setText("Your GreenHarvest one-time verification token is: " + token +
                "\n\nThis token is valid for 5 minutes.");

        mailSender.send(message);
    }
}