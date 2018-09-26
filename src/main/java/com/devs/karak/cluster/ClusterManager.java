package com.devs.karak.cluster;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
public class ClusterManager {


    public static final String KARAK_METHODS = "karak.methods";
    private CachingConnectionFactory connectionFactory;
    private RabbitTemplate template;
    private AmqpAdmin admin;
    private JobScheduler scheduler;
    private String selfIdentification;

    public ClusterManager(CachingConnectionFactory cachingConnectionFactory,
                          AmqpAdmin admin,
                          ConfigurableBeanFactory beanFactory, JobScheduler scheduler) {
        this.connectionFactory = cachingConnectionFactory;
        this.admin = admin;
        this.scheduler = scheduler;
        template = new RabbitTemplate(connectionFactory);

    }

    public void broadcastMethods(Collection<String> methods) {
        admin.declareQueue(QueueBuilder.durable(KARAK_METHODS).build());
        selfIdentification = UUID.randomUUID().toString();
        methods.stream().forEach(methodName -> transmitMessage(methodName));
    }


    private void transmitMessage(String methodName) {
        template.convertAndSend(KARAK_METHODS, (Object) methodName, new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setHeader("author", selfIdentification);
                return message;
            }
        });

    }

    @RabbitListener(queues = KARAK_METHODS)
    public void applicationArrivalListener(String topicName,
                                           Channel channel,
                                           @Header(AmqpHeaders.DELIVERY_TAG) long tag,
                                           @Header("author") String messageId) throws IOException {
        if (!selfIdentification.equals(messageId)) {
            scheduler.schedule(messageId, topicName);
            System.out.println(topicName);
            channel.basicAck(tag, false);
        } else {
            channel.basicNack(tag, false, true);


        }

    }


}
