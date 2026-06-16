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

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendBookingConfirmation(Booking booking, User customer) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customer.getEmail());
            message.setSubject("Booking Confirmation - Salon Booking System");

            // Safe way to get service name
            String serviceName = "Salon Service";
            if (booking.getService() != null) {
                try {
                    serviceName = booking.getService().getServiceName();
                } catch (Exception e) {
                    serviceName = "Service #" + booking.getService().getId();
                }
            }

            // Safe way to get staff name
            String staffName = "TBD";
            if (booking.getStaff() != null) {
                staffName = booking.getStaff().getStaffName();
            }

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
                    serviceName,
                    staffName,
                    booking.getStatus()
            );

            message.setText(content);
            mailSender.send(message);
            System.out.println("Booking confirmation sent to: " + customer.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            // Don't throw - booking is already saved
        }
    }
}