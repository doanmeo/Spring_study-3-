package com.saleticket.exam1;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class Exam1ApplicationTests {

	@Test
	void contextLoads() {
	}

}
