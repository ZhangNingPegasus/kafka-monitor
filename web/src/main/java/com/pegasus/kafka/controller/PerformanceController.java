package com.pegasus.kafka.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("performance")
public class PerformanceController {
    @RequestMapping("tolist")
    public String toList() {
        return "performance/list";
    }
}