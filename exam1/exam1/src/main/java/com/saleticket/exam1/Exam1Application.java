package com.saleticket.exam1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // cho 
public class Exam1Application {

	public static void main(String[] args) {
		SpringApplication.run(Exam1Application.class, args);
	}

}
