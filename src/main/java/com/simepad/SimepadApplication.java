package com.simepad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SimepadApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimepadApplication.class, args);
	}

}
