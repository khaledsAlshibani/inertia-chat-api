package com.inertia.chat;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {
    @GetMapping("/")
    public String home() {
        return "Inertia Chat API is running... Hello World!";
    }
}