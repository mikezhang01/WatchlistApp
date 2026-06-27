package com.cinestack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CineStackApplication {
    public static void main(String[] args) {
        SpringApplication.run(CineStackApplication.class, args);
    }
}
