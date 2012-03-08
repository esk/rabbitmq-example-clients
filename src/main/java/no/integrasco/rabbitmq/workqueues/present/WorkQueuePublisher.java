package no.integrasco.rabbitmq.workqueues.present;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class WorkQueuePublisher {

    public static final String queue = "first queue";

    public static void main(String[] args) throws IOException {

        String message = "this is a message";
        WorkQueuePublisher publisher = new WorkQueuePublisher();
        publisher.publish(message);

    }

    private void publish(String message) throws IOException {

        Channel channel = getChannel();

        ConfirmListener listener = new ConfirmListener() {

            public void handleNack(long deliveryTag, boolean multiple) throws IOException {

                // TODO Auto-generated method stub

            }

            public void handleAck(long deliveryTag, boolean multiple) throws IOException {

                // TODO Auto-generated method stub

            }
        };
        channel.addConfirmListener(listener);
        channel.confirmSelect();

        for (int i = 0; i < 150000; i++) {
            channel.basicPublish("", queue, MessageProperties.PERSISTENT_BASIC, message.getBytes());
        }
        channel.close();
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
        channel.queueDeclare(queue, durable, exclusive, autoDelete, arguments);

        return channel;
    }

}
