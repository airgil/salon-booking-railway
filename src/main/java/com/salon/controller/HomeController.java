package com.salon.controller;

import com.salon.model.SalonService;
import com.salon.model.Staff;
import com.salon.repository.ServiceRepository;
import com.salon.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private StaffRepository staffRepository;

    @GetMapping("/")
    public String home(Model model) {
        List<SalonService> services = serviceRepository.findByActiveTrue();
        List<Staff> staff = staffRepository.findAll();
        model.addAttribute("services", services);
        model.addAttribute("staff", staff);
        return "index";
    }
}