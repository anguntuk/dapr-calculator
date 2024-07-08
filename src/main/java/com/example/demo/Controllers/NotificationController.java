package com.example.demo.Controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @PostMapping("/Calculator")
    public void handleCalculationEvent(@RequestBody String message) {
        System.out.println("Received event: " + message);
    }
}
