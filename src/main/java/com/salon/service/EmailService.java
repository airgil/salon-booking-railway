package com.salon.service;

import com.salon.model.Booking;
import com.salon.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private BrevoEmailService brevoEmailService;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public void sendBookingConfirmation(Booking booking, User customer) {
        if (!emailEnabled) {
            System.out.println("📧 Email disabled - would send to: " + customer.getEmail());
            return;
        }

        if (brevoEmailService != null) {
            brevoEmailService.sendBookingConfirmation(booking, customer);
        } else {
            System.out.println("❌ No email service available");
        }
    }

    public void sendReminder(Booking booking, User customer) {
        if (!emailEnabled || brevoEmailService == null) {
            return;
        }
        brevoEmailService.sendReminder(booking, customer);
    }
}