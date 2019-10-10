package com.pegasus.kafka.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class DashboardController {
    @RequestMapping("/")
    public String test(Model model) {
        model.addAttribute("test", "你好， Kafka monitor");
        return "index";
    }
}