package com.salon.service;

import com.salon.model.Booking;
import com.salon.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    // Simple text email
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    // HTML email with template
    public void sendBookingConfirmation(Booking booking, User customer) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customer.getEmail());
            helper.setSubject("Booking Confirmation - Salon Booking System");

            Context context = new Context();
            context.setVariable("customerName", customer.getFullName());
            // FIXED: Use getDate() and getTime() (not getBookingDate/getBookingTime)
            context.setVariable("bookingDate", booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            context.setVariable("bookingTime", booking.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
            context.setVariable("status", booking.getStatus());
            context.setVariable("bookingId", booking.getId());

            // Get service and staff names
            String serviceName = booking.getService() != null ? booking.getService().getServiceName() : "N/A";
            String staffName = booking.getStaff() != null ? booking.getStaff().getStaffName() : "N/A";
            context.setVariable("service", serviceName);
            context.setVariable("staff", staffName);

            String htmlContent = templateEngine.process("email/booking-confirmation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Booking confirmation sent to: " + customer.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send booking confirmation: " + e.getMessage());
        }
    }

    public void sendBookingCancellation(Booking booking, User customer) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(customer.getEmail());
            helper.setSubject("Booking Cancellation - Salon Booking System");

            Context context = new Context();
            context.setVariable("customerName", customer.getFullName());
            // FIXED: Use getDate() and getTime()
            context.setVariable("bookingDate", booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            context.setVariable("bookingTime", booking.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
            context.setVariable("bookingId", booking.getId());

            String htmlContent = templateEngine.process("email/booking-cancellation", context);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Cancellation email sent to: " + customer.getEmail());

        } catch (Exception e) {
            System.err.println("Failed to send cancellation email: " + e.getMessage());
        }
    }

    public void sendReminder(Booking booking, User customer) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(customer.getEmail());
            message.setSubject("Reminder: Your appointment is tomorrow!");

            String serviceName = booking.getService() != null ? booking.getService().getServiceName() : "your appointment";

            message.setText(String.format(
                    "Dear %s,\n\nThis is a reminder that you have an appointment tomorrow at %s.\n\n" +
                            "Service: %s\n" +
                            "Date: %s\n" +
                            "Time: %s\n\n" +
                            "Staff: %s\n\n" +
                            "Please arrive 5 minutes early.\n\n" +
                            "To cancel or reschedule, please log into your account.\n\n" +
                            "Thank you!",
                    customer.getFullName(),
                    booking.getTime(),
                    serviceName,
                    booking.getDate(),
                    booking.getTime(),
                    booking.getStaff() != null ? booking.getStaff().getStaffName() : "TBD"
            ));
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send reminder: " + e.getMessage());
        }
    }
}