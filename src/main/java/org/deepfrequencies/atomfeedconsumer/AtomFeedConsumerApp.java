package org.deepfrequencies.atomfeedconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class AtomFeedConsumerApp 
{
	public static void main(String[] args) {
		SpringApplication.run(AtomFeedConsumerApp.class, args);
	}
}
