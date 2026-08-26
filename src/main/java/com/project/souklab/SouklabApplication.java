package com.project.souklab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SouklabApplication {

    public static void main(String[] args) {
        SpringApplication.run(SouklabApplication.class, args);
    }

}
