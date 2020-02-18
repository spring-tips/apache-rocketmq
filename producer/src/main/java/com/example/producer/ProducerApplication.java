package com.example.producer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.Message;
import org.springframework.messaging.core.MessagePostProcessor;
import org.springframework.messaging.support.MessageBuilder;

import java.time.Instant;

@RequiredArgsConstructor
@SpringBootApplication
public class ProducerApplication {

	@Bean
	ApplicationListener<ApplicationReadyEvent> ready(RocketMQTemplate template) {
		return event -> {

			var now = Instant.now();
			var destination = "greetings-topic";

			for (var name : "Tammie,Kimly,Josh,Rob,Mario,Mia".split(",")) {

				var payload = new Greeting("Hello @ " + name + " @ " + now.toString());
				var messagePostProcessor = new MessagePostProcessor() {

					@Override
					public Message<?> postProcessMessage(Message<?> message) {
						var headerValue = Character.toString(name.toLowerCase().charAt(0));
						return MessageBuilder
							.fromMessage(message)
							.setHeader("letter", headerValue)
							.build();
					}
				};
				template.convertAndSend(destination, payload, messagePostProcessor);

			}
		};
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