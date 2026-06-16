package com.salon.service;

import com.salon.model.Booking;
import com.salon.model.User;
import com.sun.jdi.connect.Transport;
import org.apache.logging.log4j.message.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.websocket.Session;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${spring.mail.password}")
    private String password;

    private Session getEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
    }

    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            Message message = new MimeMessage(getEmailSession());
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);
            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    public void sendBookingConfirmation(Booking booking, User customer) {
        String subject = "Booking Confirmation - Salon Booking System";
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
                booking.getStaff() != null ? booking.getStaff().getStaffName() : "N/A",
                booking.getStatus()
        );
        sendSimpleEmail(customer.getEmail(), subject, content);
    }

    public void sendReminder(Booking booking, User customer) {
        String subject = "Reminder: Your appointment is tomorrow!";
        String serviceName = booking.getService() != null ? booking.getService().getServiceName() : "your appointment";

        String content = String.format(
                "Dear %s,\n\n" +
                        "This is a reminder that you have an appointment tomorrow.\n\n" +
                        "Service: %s\n" +
                        "Date: %s\n" +
                        "Time: %s\n" +
                        "Staff: %s\n\n" +
                        "Please arrive 5 minutes early.\n\n" +
                        "Thank you!",
                customer.getFullName(),
                serviceName,
                booking.getDate(),
                booking.getTime(),
                booking.getStaff() != null ? booking.getStaff().getStaffName() : "TBD"
        );
        sendSimpleEmail(customer.getEmail(), subject, content);
    }
}