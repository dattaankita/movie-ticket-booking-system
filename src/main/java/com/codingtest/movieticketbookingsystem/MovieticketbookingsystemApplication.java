package com.codingtest.movieticketbookingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
class MovieTicketBookingApplication {
	public static void main(String[] args) {
        SpringApplication.run(MovieTicketBookingApplication.class, args);
	}
}