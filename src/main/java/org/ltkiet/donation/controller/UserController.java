package org.ltkiet.donation.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/public")
public class UserController {

    @GetMapping("/login")
    public String login() {
        return "public/login"; // Matches public/login.html
    }
}