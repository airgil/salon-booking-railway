package com.salon.controller;

import com.salon.model.User;
import com.salon.service.UserService;
import com.salon.service.EmailService;
import com.salon.service.BrevoEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private BrevoEmailService brevoEmailService;

    // ===== GET MAPPINGS =====

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "success", required = false) String success,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (success != null) {
            model.addAttribute("success", "Registration successful! Please login.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout=true";
    }

    // ===== POST MAPPINGS =====

    @PostMapping("/login")
    public String doLogin(@RequestParam String username,
                          @RequestParam String password,
                          HttpSession session,
                          Model model) {
        User user = userService.login(username, password);

        if (user != null) {
            session.setAttribute("user", user);
            if ("admin".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/customer/dashboard";
        }

        model.addAttribute("error", "Invalid username or password");
        return "login";
    }

    @PostMapping("/register")
    public String doRegister(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam(required = false) String phone,
                             Model model) {
        // Check if username already exists
        if (userService.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username already exists");
            return "register";
        }

        // Check if email already exists
        if (userService.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email already registered");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setRole("customer");

        if (userService.register(user)) {
            return "redirect:/login?success=true";
        }

        model.addAttribute("error", "Registration failed. Please try again.");
        return "register";
    }

    // ===== EMAIL TEST ENDPOINTS =====

    @GetMapping("/email-status")
    @ResponseBody
    public String emailStatus() {
        return "Email Service Status:\n" +
                "BrevoEmailService: " + (brevoEmailService != null ? "✅ Available" : "❌ NULL") + "\n" +
                "EmailService: " + (emailService != null ? "✅ Available" : "❌ NULL");
    }

    @GetMapping("/test-email-simple")
    @ResponseBody
    public String testEmailSimple(@RequestParam String email) {
        if (brevoEmailService == null) {
            return "❌ BrevoEmailService is NULL. Check dependency.";
        }

        try {
            com.salon.model.Booking testBooking = new com.salon.model.Booking();
            testBooking.setId(999L);
            testBooking.setDate(java.time.LocalDate.now());
            testBooking.setTime(java.time.LocalTime.now());
            testBooking.setStatus("test");

            User testUser = new User();
            testUser.setEmail(email);
            testUser.setFullName("Test User");

            brevoEmailService.sendBookingConfirmation(testBooking, testUser);
            return "✅ Test email sent to: " + email + ". Check your inbox and spam folder!";
        } catch (Exception e) {
            return "❌ Email failed: " + e.getMessage();
        }
    }
}