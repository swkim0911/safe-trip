package com.swkim.safetrip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class SafetripApplication {

	public static void main(String[] args) {
		SpringApplication.run(SafetripApplication.class, args);
	}

}
