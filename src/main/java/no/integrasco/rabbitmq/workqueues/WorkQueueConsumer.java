package no.integrasco.rabbitmq.workqueues;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class WorkQueueConsumer {

    public static void main(String[] args) throws IOException, ShutdownSignalException,
            InterruptedException {

        WorkQueueConsumer consumer = new WorkQueueConsumer();
        consumer.consume();
    }

    private Channel getChannel() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // survive broker restart
        boolean durable = true;

        // there can only be one client for this specific queue : false
        boolean exclusive = false;

        // the exchange will get deleted as soon as there are no more queues
        // bound to it
        boolean autoDelete = false;

        Map<String, Object> rabbitMqProperties = new HashMap<String, Object>();

        // mirroring to all brokers in the cluster
        final String MIRROR_MESSAGE_POLICY = "x-ha-policy";
        final String APPLY_TO_ALL_QUEUES = "all";
        rabbitMqProperties.put(MIRROR_MESSAGE_POLICY, APPLY_TO_ALL_QUEUES);

        channel.queueDeclare(WorkQueuePublisher.QUEUE_NAME, durable, exclusive, autoDelete,
                rabbitMqProperties);

        return channel;
    }

    private void consume() throws IOException, InterruptedException {

        Channel channel = getChannel();

        // setting prefetch count
        channel.basicQos(100);

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(WorkQueuePublisher.QUEUE_NAME, false, consumer);

        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        String message = new String(delivery.getBody());
        doWork(message);

        long deliveryTag = delivery.getEnvelope().getDeliveryTag();
        channel.basicAck(deliveryTag, false);

        channel.close();
    }

    private static void doWork(String message) {

        System.out.println("Received: " + message);
    }
}
