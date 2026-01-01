package com.example.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
        System.out.println("TEST DB PASSWORD: " + System.getenv("DB_PASSWORD"));
        SpringApplication.run(Application.class, args);
	}

}
