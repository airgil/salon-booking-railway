package com.salon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SalonBookingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalonBookingApplication.class, args);
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║   ✂️ Salon Booking System Started ✂️   ║");
        System.out.println("║   http://localhost:8080                ║");
        System.out.println("╚════════════════════════════════════════╝");
    }
}