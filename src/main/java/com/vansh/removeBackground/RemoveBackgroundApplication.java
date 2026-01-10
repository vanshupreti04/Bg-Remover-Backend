package com.vansh.removeBackground;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RemoveBackgroundApplication {

	public static void main(String[] args) {
		SpringApplication.run(RemoveBackgroundApplication.class, args);
	}

}
