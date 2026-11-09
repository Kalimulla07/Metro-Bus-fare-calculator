package com.transit.farecalculator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Entry Point for Metro & Bus Fare Calculator Application.
 * Spring Boot starts the embedded web server and initializes all components.
 */
@SpringBootApplication
public class MetroFareCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(MetroFareCalculatorApplication.class, args);
        System.out.println("==================================================================");
        System.out.println(" Metro & Bus Fare Calculator application started successfully! ");
        System.out.println(" Open your browser and navigate to: http://localhost:8080        ");
        System.out.println("==================================================================");
    }
}
