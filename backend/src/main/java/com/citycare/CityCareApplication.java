package com.citycare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * CityCare Application Entrypoint
 * Problem: SIH25031 - Crowdsourced Civic Issue Reporting & Resolution System
 */
@SpringBootApplication
@EnableAsync
public class CityCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(CityCareApplication.class, args);
    }
}
