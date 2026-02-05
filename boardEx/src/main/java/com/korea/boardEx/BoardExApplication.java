package com.korea.boardEx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication//(exclude={DataSourceAutoConfiguration.class})
public class BoardExApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardExApplication.class, args);
	}

}
