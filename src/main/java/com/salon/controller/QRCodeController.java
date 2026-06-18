package com.salon.controller;

import com.salon.model.Booking;
import com.salon.model.User;
import com.salon.repository.BookingRepository;
import com.salon.service.QRCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/qr")
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/view/{id}")
    public String viewQR(@PathVariable Long id, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null || !booking.getUser().getId().equals(user.getId())) {
            return "redirect:/my-bookings";
        }

        model.addAttribute("booking", booking);
        model.addAttribute("qrImageUrl", "/qr/booking/" + id);
        return "qr-view";
    }

    @GetMapping("/booking/{id}")
    public ResponseEntity<byte[]> generateBookingQR(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null || !booking.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            byte[] qrCode = qrCodeService.generateBookingQRCode(booking);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("filename", "booking_qr_" + id + ".png");

            return new ResponseEntity<>(qrCode, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/checkin/{id}")
    public String checkIn(@PathVariable Long id, Model model) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        if (booking == null) {
            return "redirect:/";
        }

        if ("confirmed".equals(booking.getStatus())) {
            booking.setStatus("completed");
            bookingRepository.save(booking);
            model.addAttribute("message", "✅ Check-in successful! Welcome!");
        } else if ("completed".equals(booking.getStatus())) {
            model.addAttribute("message", "⏳ You have already checked in.");
        } else if ("cancelled".equals(booking.getStatus())) {
            model.addAttribute("message", "❌ This booking has been cancelled.");
        } else {
            model.addAttribute("message", "⏳ Your booking is pending confirmation.");
        }

        model.addAttribute("booking", booking);
        return "checkin-success";
    }
}