package com.mark.convert.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.mark.convert.core")
@EnableScheduling
public class ConvertCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConvertCoreApplication.class, args);
    }

}
