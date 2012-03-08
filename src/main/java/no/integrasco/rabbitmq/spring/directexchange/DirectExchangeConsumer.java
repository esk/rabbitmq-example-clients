package no.integrasco.rabbitmq.spring.directexchange;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class DirectExchangeConsumer {

    public static void main(String[] args) {

        ConnectionFactory connectionFactory = new CachingConnectionFactory();

        AmqpAdmin admin = new RabbitAdmin(connectionFactory);
        Queue queue = new Queue(DirectExchangePublisher.QUEUE_NAME);

        admin.declareQueue(queue);

        AmqpTemplate template = new RabbitTemplate(connectionFactory);

        String receivedMessage = (String) template
                .receiveAndConvert(DirectExchangePublisher.QUEUE_NAME);
        System.out.println("Received: " + receivedMessage);
    }
}
