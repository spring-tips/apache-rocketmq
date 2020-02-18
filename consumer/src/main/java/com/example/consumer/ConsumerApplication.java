package com.example.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Service;

import static org.apache.rocketmq.spring.annotation.SelectorType.SQL92;

@SpringBootApplication
public class ConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}
}

@Log4j2
@Service
@RocketMQMessageListener(
	topic = "greetings-topic",
	selectorExpression = " letter = 'm' ",
	selectorType = SQL92,
	consumerGroup = "greetings-consumer-group"
)
class GreetingsConsumer implements RocketMQListener<Greeting> {

	@Override
	public void onMessage(Greeting greeting) {
		log.info("new message: " + greeting.toString());
	}
}


@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}