package com.salon.controller;

import com.salon.model.User;
import com.salon.service.UserService;
import com.salon.service.EmailService;
import com.salon.service.BrevoEmailService;
import com.salon.util.RateLimiter;  // ← ADD THIS IMPORT
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;  // ← ADD THIS IMPORT
import javax.servlet.http.HttpSession;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired(required = false)
    private BrevoEmailService brevoEmailService;

    @Autowired
    private RateLimiter rateLimiter;  // ← ADD THIS

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
                          HttpServletRequest request,  // ← ADD THIS
                          HttpSession session,
                          Model model) {
        // Rate limiting - get client IP
        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.allowRequest(clientIp)) {
            model.addAttribute("error", "Too many login attempts. Please try again later.");
            return "login";
        }

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
        // Sanitize inputs
        username = sanitizeInput(username);
        fullName = sanitizeInput(fullName);
        email = sanitizeInput(email);
        phone = sanitizeInput(phone);

        // Validate password strength
        if (!isPasswordStrong(password)) {
            model.addAttribute("error", "Password must be at least 8 characters with uppercase, lowercase, and numbers");
            return "register";
        }

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

    // ===== HELPER METHODS =====

    private String sanitizeInput(String input) {
        if (input == null) return null;
        // Remove HTML tags and script tags
        return input.replaceAll("(?i)<script.*?>.*?</script.*?>", "")
                .replaceAll("(?i)<.*?>", "")
                .trim();
    }

    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 8) return false;

        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasLowercase = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");

        return hasUppercase && hasLowercase && hasNumber && hasSpecial;
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
            // Create a test booking
            com.salon.model.Booking testBooking = new com.salon.model.Booking();
            testBooking.setId(999L);
            testBooking.setDate(java.time.LocalDate.now());
            testBooking.setTime(java.time.LocalTime.now());
            testBooking.setStatus("test");

            // Create a test user
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