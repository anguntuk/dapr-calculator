package com.example.demo.Controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus; // Add this import

import com.example.demo.Models.Calculator;

import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import io.dapr.client.domain.State;
import reactor.core.publisher.Mono;

@Controller
public class CalculatorController {

    private static final String STATE_STORE_NAME = "statestore";
    private final DaprClient client;

    public CalculatorController() {
        this.client = new DaprClientBuilder().build();
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("operator", "+");
        model.addAttribute("view", "views/calculatorForm");
        return "base-layout";
    }

    @PostMapping("/")
    public String index(
            @RequestParam String leftOperand,
            @RequestParam String operator,
            @RequestParam String rightOperand,
            Model model
    ) {
        double leftNumber;
        double rightNumber;

        try {
            leftNumber = Double.parseDouble(leftOperand);
        } catch (NumberFormatException ex) {
            leftNumber = 0;
        }

        try {
            rightNumber = Double.parseDouble(rightOperand);
        } catch (NumberFormatException ex) {
            rightNumber = 0;
        }

        Calculator calculator = new Calculator(
                leftNumber,
                rightNumber,
                operator
        );

        double result = calculator.calculateResult();
        try {
            client.saveState(STATE_STORE_NAME, "lastResult", result).block();
            client.publishEvent("pubsub", "calculation", "Result: " + result).block();
        } catch (Exception e) {
            System.err.println("Error saving state or publishing event: " + e.getMessage());
            e.printStackTrace();
        }

        model.addAttribute("leftOperand", leftNumber);
        model.addAttribute("operator", operator);
        model.addAttribute("rightOperand", rightNumber);
        model.addAttribute("result", result);

        model.addAttribute("view", "views/calculatorForm");
        return "base-layout";
    }

    @PostMapping("/Calculator")
    @ResponseBody
    public double calculate(@RequestBody Calculator calculator) {
        double result = calculator.calculateResult();
        try {
            client.saveState(STATE_STORE_NAME, "lastResult", result).block();
            client.publishEvent("pubsub", "calculation", "Result: " + result).block();
        } catch (Exception e) {
            System.err.println("Error saving state or publishing event: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    @GetMapping("/lastResult")
    @ResponseBody
    public double getLastResult() {
        double lastResult = 0.0;
        try {
            DaprClient client = new DaprClientBuilder().build();
            Mono<State<Double>> stateResponseMono = client.getState(STATE_STORE_NAME, "lastResult", Double.class);
            State<Double> stateResponse = stateResponseMono.blockOptional().orElse(null);
            
            if (stateResponse != null) {
                lastResult = stateResponse.getValue(); // Retrieve the actual value from stateResponse
            } else {
                System.err.println("No state found for key 'lastResult'");
            }
        } catch (Exception e) {
            System.err.println("Error retrieving last result from state store: " + e.getMessage());
            e.printStackTrace();
        }
        
        return lastResult; // Return the retrieved lastResult (either actual value or default 0.0)
    }




    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }
}
