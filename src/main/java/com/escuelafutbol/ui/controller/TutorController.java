package com.escuelafutbol.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tutor")
public class TutorController {

    @GetMapping("/panel")
    public String panel() {
        return "tutor/panel";
    }
}