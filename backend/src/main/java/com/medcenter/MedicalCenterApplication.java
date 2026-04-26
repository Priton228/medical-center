package com.medcenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MedicalCenterApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalCenterApplication.class, args);
    }
}
