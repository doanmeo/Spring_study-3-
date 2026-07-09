package com.saleticket.exam1;

import org.springframework.boot.SpringApplication;

public class TestExam1Application {

	public static void main(String[] args) {
		SpringApplication.from(Exam1Application::main).with(TestcontainersConfiguration.class).run(args);
	}

}
