package no.integrasco.rabbitmq.spring.directexchange;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class DirectExchangePublisher {

    public static final String EXCHANGE_NAME = "direct exchange";

    public static final String QUEUE_NAME = "direct exchange queue";

    private static final String ROUTING_KEY = "black";

    public static void main(String[] args) {

        ConnectionFactory connectionFactory = new CachingConnectionFactory();

        AmqpAdmin admin = new RabbitAdmin(connectionFactory);
        Queue queue = new Queue(QUEUE_NAME);

        admin.declareQueue(queue);

        Exchange exchange = new DirectExchange(EXCHANGE_NAME);

        admin.declareExchange(exchange);

        Binding binding = new Binding(QUEUE_NAME, Binding.DestinationType.QUEUE, EXCHANGE_NAME,
                ROUTING_KEY, null);

        admin.declareBinding(binding);

        AmqpTemplate template = new RabbitTemplate(connectionFactory);

        String message = "this is the message";
        template.convertAndSend(EXCHANGE_NAME, ROUTING_KEY, message);

        System.out.println("Sent: " + message);
    }
}