package com.studypulse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudyPulseApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyPulseApplication.class, args);
        System.out.println("=================================================");
        System.out.println("🚀 StudyPulse Backend is running at http://localhost:8080");
        System.out.println("📱 Open http://localhost:8080 in your browser or mobile");
        System.out.println("=================================================");
    }
}
