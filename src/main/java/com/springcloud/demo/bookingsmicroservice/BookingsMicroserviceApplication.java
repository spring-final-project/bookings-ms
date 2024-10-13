package com.springcloud.demo.bookingsmicroservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BookingsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingsMicroserviceApplication.class, args);
	}

}
