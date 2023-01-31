package com.example.javakinesisconsumer;

import com.example.javakinesisconsumer.config.DynamoDbConfiguration;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@Log4j2
@Import(value = {DynamoDbConfiguration.class})
@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
