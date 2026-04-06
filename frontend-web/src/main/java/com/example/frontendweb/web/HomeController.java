package com.example.frontendweb.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Value("${backend.api.base-url}")
    private String apiBase;

    @Value("${app.google-client-id:}")
    private String googleClientId;

    private void authModel(Model model) {
        model.addAttribute("apiBase", apiBase);
        model.addAttribute("googleClientId", googleClientId != null ? googleClientId : "");
    }

    @GetMapping({"/", "/index"})
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(Model model) {
        authModel(model);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        authModel(model);
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        authModel(model);
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(Model model) {
        authModel(model);
        return "reset-password";
    }

    @GetMapping("/trips")
    public String trips() {
        return "trips";
    }

    @GetMapping("/booking")
    public String booking() {
        return "booking";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        authModel(model);
        return "admin/dashboard";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }

    @GetMapping("/refund-policy")
    public String refundPolicy() {
        return "refund-policy";
    }

    @GetMapping("/payment")
    public String payment() {
        return "payment";
    }

    @GetMapping("/seats")
    public String seats() {
        return "seats";
    }

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @GetMapping("/my-tickets")
    public String myTickets(Model model) {
        authModel(model);
        return "my-tickets";
    }
}
