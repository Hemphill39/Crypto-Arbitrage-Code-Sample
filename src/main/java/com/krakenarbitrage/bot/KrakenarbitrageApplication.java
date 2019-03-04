package com.krakenarbitrage.bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration
public class KrakenarbitrageApplication {

	public static void main(String[] args) {
		SpringApplication.run(KrakenarbitrageApplication.class, args);
	}
}
