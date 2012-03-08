package no.integrasco.rabbitmq.workqueues.present;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class WorkQueueConsumer {

    public static void main(String[] args) throws IOException, ShutdownSignalException,
            ConsumerCancelledException, InterruptedException {

        WorkQueueConsumer consumer = new WorkQueueConsumer();
        consumer.consume();
    }

    private void consume() throws IOException, ShutdownSignalException, ConsumerCancelledException,
            InterruptedException {

        Channel channel = getChannel();

        channel.basicQos(1);

        QueueingConsumer consumer = new QueueingConsumer(channel);

        channel.basicConsume(WorkQueuePublisher.queue, false, consumer);

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();

        String receivedMessage = new String(delivery.getBody());
        System.out.println("Received: " + receivedMessage);

        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        boolean multiple = false;
        channel.basicAck(deliveryTag, multiple);

    }

    private Channel getChannel() throws IOException {

        ConnectionFactory connectionFactory = new ConnectionFactory();

        connectionFactory.setHost("localhost");
        Connection connection = connectionFactory.newConnection();

        Channel channel = connection.createChannel();

        boolean durable = true;
        boolean exclusive = false;
        boolean autoDelete = false;

        Map<String, Object> arguments = new HashMap<String, Object>();
        String mirrorMessagePolicy = "x-ha-policy";
        String applyToAllQueues = "all";
        arguments.put(mirrorMessagePolicy, applyToAllQueues);

        channel.queueDeclare(WorkQueuePublisher.queue, durable, exclusive, autoDelete, arguments);

        return channel;
    }

}
