package no.integrasco.rabbitmq.spring.simple;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;

public class SimpleSpringRabbitMQClient {

    public static void main(String[] args) {

        ApplicationContext context = new GenericXmlApplicationContext(
                "classpath:/rabbit-context.xml");
        AmqpTemplate template = context.getBean(AmqpTemplate.class);

        template.convertAndSend("simple.queue", "this is a message");

        String receivedMessage = (String) template.receiveAndConvert("simple.queue");
        System.out.println(receivedMessage);

    }
}
