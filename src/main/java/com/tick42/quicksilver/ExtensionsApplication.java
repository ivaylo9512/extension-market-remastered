package com.tick42.quicksilver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExtensionsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExtensionsApplication.class, args);
    }
}
