package com.bank.credit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableMongoAuditing
public class MsCreditApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsCreditApplication.class, args);
	}
	/**
	 * Provides a RestTemplate bean for inter-service communication.
	 *
	 * @return a new RestTemplate instance
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
