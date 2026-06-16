package com.salon.service;

import com.salon.model.Booking;
import com.salon.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendBookingConfirmation(Booking booking, User customer) {
        if (!emailEnabled || mailSender == null) {
            System.out.println("📧 Email disabled or mailSender not configured. Would send to: " + customer.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customer.getEmail());
            message.setSubject("Booking Confirmation - Salon Booking System");

            String content = String.format(
                    "Dear %s,\n\n" +
                            "Your booking has been confirmed!\n\n" +
                            "Booking ID: #%d\n" +
                            "Date: %s\n" +
                            "Time: %s\n" +
                            "Service: %s\n" +
                            "Staff: %s\n" +
                            "Status: %s\n\n" +
                            "Thank you for choosing us!",
                    customer.getFullName(),
                    booking.getId(),
                    booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    booking.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                    booking.getService() != null ? booking.getService().getServiceName() : "N/A",
                    booking.getStaff() != null ? booking.getStaff().getStaffName() : "TBD",
                    booking.getStatus()
            );

            message.setText(content);
            mailSender.send(message);
            System.out.println("✅ Booking confirmation sent to: " + customer.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
        }
    }

    // ADD THIS METHOD - for reminders
    public void sendReminder(Booking booking, User customer) {
        if (!emailEnabled || mailSender == null) {
            System.out.println("📧 Reminder disabled - would send to: " + customer.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customer.getEmail());
            message.setSubject("Reminder: Your appointment is tomorrow!");

            String content = String.format(
                    "Dear %s,\n\n" +
                            "This is a reminder that you have an appointment tomorrow.\n\n" +
                            "Date: %s\n" +
                            "Time: %s\n\n" +
                            "Service: %s\n" +
                            "Staff: %s\n\n" +
                            "Please arrive 5 minutes early.\n\n" +
                            "Thank you!",
                    customer.getFullName(),
                    booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    booking.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                    booking.getService() != null ? booking.getService().getServiceName() : "N/A",
                    booking.getStaff() != null ? booking.getStaff().getStaffName() : "TBD"
            );

            message.setText(content);
            mailSender.send(message);
            System.out.println("✅ Reminder sent to: " + customer.getEmail());

        } catch (Exception e) {
            System.err.println("❌ Failed to send reminder: " + e.getMessage());
        }
    }
}