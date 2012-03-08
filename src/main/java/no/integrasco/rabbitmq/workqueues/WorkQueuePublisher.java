package no.integrasco.rabbitmq.workqueues;

import static java.util.Collections.synchronizedSortedMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmListener;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class WorkQueuePublisher {

    public static final String QUEUE_NAME = "task_queue";

    private final SortedMap<Long, String> unConfirmedMessageDeliveryTags = synchronizedSortedMap(new TreeMap<Long, String>());

    Queue<ResendMessage> resendMessages = new ConcurrentLinkedQueue<ResendMessage>();

    public static void main(String[] args) throws IOException {

        String message = "this is a message";

        WorkQueuePublisher publisher = new WorkQueuePublisher();
        publisher.publish(message);
    }

    private Channel getChannel() throws IOException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        Map<String, Object> rabbitMqProperties = new HashMap<String, Object>();

        // mirroring to all other machines in the cluseter
        final String MIRROR_MESSAGE_POLICY = "x-ha-policy";
        final String APPLY_TO_ALL_QUEUES = "all";
        rabbitMqProperties.put(MIRROR_MESSAGE_POLICY, APPLY_TO_ALL_QUEUES);

        // survive broker restart
        boolean durable = true;

        // there can only be one client for this specific queue : false
        boolean exclusive = false;

        // the exchange will get deleted as soon as there are no more queues
        // bound to it
        boolean autoDelete = false;

        channel.queueDeclare(WorkQueuePublisher.QUEUE_NAME, durable, exclusive, autoDelete,
                rabbitMqProperties);

        return channel;
    }

    private void publish(String message) throws IOException {

        Channel channel = getChannel();
        channel.confirmSelect();

        ConfirmListener confirmListener = new ConfirmListener() {

            public void handleNack(long deliveryTag, boolean multiple) throws IOException {

                String message = unConfirmedMessageDeliveryTags.get(deliveryTag);
                if (message != null) {
                    resendMessages.add(new ResendMessage(message, QUEUE_NAME));
                    // Messages in resendMessages will be sent later in its own
                    // thread
                }
            }

            public void handleAck(long deliveryTag, boolean multiple) throws IOException {

                if (multiple) {
                    unConfirmedMessageDeliveryTags.headMap(deliveryTag + 1).clear();
                } else {
                    unConfirmedMessageDeliveryTags.remove(deliveryTag);
                }
            }
        };

        // Enables publisher acknowledgements on this channel
        channel.addConfirmListener(confirmListener);

        long deliveryTag = channel.getNextPublishSeqNo();
        unConfirmedMessageDeliveryTags.put(deliveryTag, message);

        channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_BASIC, message.getBytes());
        System.out.println("Published: " + message);

        channel.close();

    }
}
