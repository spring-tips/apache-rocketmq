package com.example.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.time.Instant;

@RequiredArgsConstructor
@SpringBootApplication
public class ProducerApplication {

	private final RocketMQTemplate rocketMQTemplate;

	@EventListener(ApplicationReadyEvent.class)
	public void run() {
		var now = Instant.now();
		for (var name : "Tammie,Kimly,Josh,Rob,Mario,Mia".split(",")) {
			this.rocketMQTemplate.convertAndSend(
				"greetings-topic", new Greeting("Hello @ " + name + " @ " + now.toString()));
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);
	}
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}