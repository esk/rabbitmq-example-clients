package no.integrasco.rabbitmq.workqueues;

public class ResendMessage {

    private final String message;

    private final String queue;

    public ResendMessage(final String message, final String queue) {

        this.message = message;
        this.queue = queue;

    }

    public String getMessage() {

        return message;
    }

    public String getQueue() {

        return queue;
    }
}
