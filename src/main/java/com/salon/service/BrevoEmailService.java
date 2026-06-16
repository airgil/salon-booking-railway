package com.salon.service;

import com.salon.model.Booking;
import com.salon.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Service
public class BrevoEmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public void sendBookingConfirmation(Booking booking, User customer) {
        if (!emailEnabled) {
            System.out.println("📧 Email disabled - would send to: " + customer.getEmail());
            return;
        }

        if (apiKey == null || apiKey.isEmpty()) {
            System.err.println("❌ BREVO_API_KEY not set in environment variables");
            return;
        }

        try {
            String content = String.format(
                    "Dear %s,\n\n" +
                            "Your booking has been confirmed!\n\n" +
                            "Booking ID: #%d\n" +
                            "Date: %s\n" +
                            "Time: %s\n" +
                            "Service: %s\n" +
                            "Staff: %s\n" +
                            "Status: %s\n\n" +
                            "Thank you for choosing us!\n\n" +
                            "Best regards,\n" +
                            "Salon Booking System",
                    customer.getFullName(),
                    booking.getId(),
                    booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    booking.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")),
                    booking.getService() != null ? booking.getService().getServiceName() : "N/A",
                    booking.getStaff() != null ? booking.getStaff().getStaffName() : "TBD",
                    booking.getStatus()
            );

            // Escape JSON strings
            String escapedContent = content
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String jsonPayload = String.format(
                    "{\"sender\":{\"email\":\"%s\"},\"to\":[{\"email\":\"%s\",\"name\":\"%s\"}],\"subject\":\"Booking Confirmation - Salon Booking System\",\"textContent\":\"%s\"}",
                    fromEmail,
                    customer.getEmail(),
                    customer.getFullName(),
                    escapedContent
            );

            URL url = new URL("https://api.brevo.com/v3/smtp/email");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("✅ Booking confirmation sent to: " + customer.getEmail());
            } else {
                System.err.println("❌ Email failed with code: " + responseCode);
                try (java.util.Scanner s = new java.util.Scanner(conn.getErrorStream(), "UTF-8").useDelimiter("\\A")) {
                    System.err.println("Error: " + (s.hasNext() ? s.next() : ""));
                }
            }
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Email failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendReminder(Booking booking, User customer) {
        if (!emailEnabled || apiKey == null || apiKey.isEmpty()) {
            return;
        }

        try {
            String content = String.format(
                    "Dear %s,\n\n" +
                            "This is a reminder that you have an appointment tomorrow.\n\n" +
                            "Date: %s\n" +
                            "Time: %s\n\n" +
                            "Please arrive 5 minutes early.\n\n" +
                            "Thank you!",
                    customer.getFullName(),
                    booking.getDate().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
                    booking.getTime().format(DateTimeFormatter.ofPattern("hh:mm a"))
            );

            String escapedContent = content
                    .replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n");

            String jsonPayload = String.format(
                    "{\"sender\":{\"email\":\"%s\"},\"to\":[{\"email\":\"%s\",\"name\":\"%s\"}],\"subject\":\"Reminder: Your appointment is tomorrow!\",\"textContent\":\"%s\"}",
                    fromEmail,
                    customer.getEmail(),
                    customer.getFullName(),
                    escapedContent
            );

            URL url = new URL("https://api.brevo.com/v3/smtp/email");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("api-key", apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                System.out.println("✅ Reminder sent to: " + customer.getEmail());
            } else {
                System.err.println("❌ Reminder failed with code: " + responseCode);
            }
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Reminder failed: " + e.getMessage());
        }
    }
}