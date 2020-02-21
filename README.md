# Spring Tips (Apache RocketMQ)

## Running Apache RocketMQ 

In order to use this you'll need to follow the steps in the [RocketMQ](https://rocketmq.apache.org/docs/quick-start/) quickstart. This Spring Tips installment introduces Apache RocketMQ, originally a technology developed and used internally at Alibaba and proven in the forge of 11/11, the famous Chinese sales holiday, sort of like "cyber Monday," or "Black Friday," in the US. Sort of like that, but waaay bigger. In 2019, Alibaba (alone, with no other e-commerce engines involved), made almost $40 billion USD in 24 hours. This required trillions of messages be sent through something that could scale to meet the demand. RocketMQ is the only thing they could trust. 

You'll need to use Java 8 when running Apache RocketMQ. (You can use any version of Java when writing Spring applications that connect to Apache RocketMQ, of course.) I use SDK Manager (`sdk`) to switch to the appropriate version of Java.

```
sdk use java 8.0.242.hs-adpt
```

That'll install a version that works if it's not already installed. Once that's done, you'll then need to run the NameServer.

```
${ROCKETMQ_HOME}/bin/mqnamesrv 
```

Then you'll need to run the Broker itself.

```
${ROCKETMQ_HOME}/bin/mqbroker -n localhost:9876
```

If you want to use SQL-based filtering, you need to add a property to the broker's configuration, `$ROCKETMQ_HOME/conf/broker.conf`, and then tell RocketMQ to use that configuration.

```
enablePropertyFilter = true
```

I use a script like this to launch everything.

``` 
export JAVA_HOME=$HOME/.sdkman/candidates/java/8.0.242.hs-adpt
${ROCKETMQ_HOME}/bin/mqnamesrv &  
${ROCKETMQ_HOME}/bin/mqbroker -n localhost:9876 -c ${ROCKETMQ_HOME}/conf/broker.conf
```



## Using Apache RocketMQ from Java Code 

Let's look at a simple producer class that uses the Spring Boot autoconfiguration and the `RocketMQTemplate`. 

In order to work with this, you'll need to create a new project on the [Spring Initializr](http://start.Spring.io). I generated a new project with the latest version of Java and then I made sure to include `Lombok`. We also need the Apache RocketMQ client and the appropriate Spring Boot autoconfiguration:

```xml
<dependency>
	<groupId>org.apache.rocketmq</groupId>
	<artifactId>rocketmq-spring-boot-starter</artifactId>
	<version>2.0.4</version>
</dependency>
```

The autoconfiguration will create a connection to the running Apache RocketMQ broker, informed by certain properties. 

```properties
rocketmq.name-server=127.0.0.1:9876
rocketmq.producer.group=greetings-producer-group
```

The first property, `name-server`, tells the application where the Apache RocketMQ nameserver lives. The nameserver, then, knows where the broker lives. You'll need to also specify a group for both the producer and the consumer. Here, we use `greetings-producer-group`. 


```java

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
```

I don't know if it can get much simpler than that! It's a simple for-loop, processing each name, creating a new `Greeting` object, and then using the `RocketMQTemplate` to send the payload to an Apache RocketMQ topic, `greetings-topic`. Here, we've used the overload of the `RocketMQTemplate` object that accepts a `MessagePostProcessor`. The `MessagePostProcessor` is a callback in which we can transform the Spring Framework `Message` object that will be sent out. In this example, we contribute a header value, `letter`, that contains the first letter of the name. We'll use this in the consumer.

Let's look at the consumer. Generate a new Spring Boot application from the Spring Initializr and be sure to add the Apache RocketMQ autoconfiguration. You'll need to specify the name server in `application.properties` for the client, too. 

The autoconfiguration supports defining beans that implement `RocketMQListener<T>`, where `T` is the type of the payload that the consumer will receive. The payload, in this case, is the `Greeting`.

```java
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

@Data
@AllArgsConstructor
@NoArgsConstructor
class Greeting {
	private String message;
}

@Log4j2
@Service
@RocketMQMessageListener(
	topic = "greetings-topic",
	consumerGroup = "simple-group"
)
class SimpleConsumer implements RocketMQListener<Greeting> {

	@Override
	public void onMessage(Greeting greeting) {
		log.info(greeting.toString());
	}
}

```	

In this example, the `SimpleConsumer` simply logs all incoming messages from the `greetings-topic` topic in Apache RocketMQ. Here, the consumer will process _every_ message on the topic. Let's look at another nice feature - selectors  - that let us selectively process incoming messages. Let's replace the existing RocketMQ listener with two new ones. Each one will use a SQL92-compatible predicate to determine whether incoming messages should be processed. One listener processes only the messages that have a `letter` header matching `m`, `k`, or `t`. The other matches only those whose `letter` header matches `j`. 

```java

@Log4j2
@Service
@RocketMQMessageListener(
	topic = "greetings-topic",
	selectorExpression = " letter = 'm' or letter = 'k' or letter = 't' ",
	selectorType = SQL92,
	consumerGroup = "sql-consumer-group-mkt"
)
class MktSqlSelectorConsumer implements RocketMQListener<Greeting> {

	@Override
	public void onMessage(Greeting greeting) {
		log.info("'m', 'k', 't': " + greeting.toString());
	}
}


@Log4j2
@Service
@RocketMQMessageListener(
	topic = "greetings-topic",
	selectorExpression = " letter = 'j' ",
	selectorType = SQL92,
	consumerGroup = "sql-consumer-group-j"
)
class JSqlSelectorConsumer implements RocketMQListener<Greeting> {

	@Override
	public void onMessage(Greeting greeting) {
		log.info("'j': " + greeting.toString());
	}
}

```

Not bad, eh? There's plenty of other things that Apache RocketMQ supports (besides processing trillions of messages in 24 hours!) It can store long tail messages on disk, without degrading performance. It supports serialization - the ordering of - of messages, transactions,  batch processing, etc. It even supports scheduled messages - messages that are only delivered after a certain interval. Needless to say, I'm a big Apache RocketMQ fan. 










