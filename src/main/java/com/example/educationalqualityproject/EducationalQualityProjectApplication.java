package com.example.educationalqualityproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class EducationalQualityProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(EducationalQualityProjectApplication.class, args);
    }

}