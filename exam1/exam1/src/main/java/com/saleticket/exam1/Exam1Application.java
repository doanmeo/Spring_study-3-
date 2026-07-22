package com.saleticket.exam1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing // cho phép sử dụng Auditing
@EnableScheduling // cho phép sử dụng Scheduling
public class Exam1Application {

	public static void main(String[] args) {
		SpringApplication.run(Exam1Application.class, args);
	}

}
