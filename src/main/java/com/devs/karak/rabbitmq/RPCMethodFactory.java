package com.devs.karak.rabbitmq;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import com.devs.karak.cluster.ClusterManager;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.stereotype.Service;

import static java.util.Arrays.asList;

@Service
public class RPCMethodFactory {

    private static final String RPC_LISTENER_NAME_TEMPLATE = "rpc_method_for_%s";
    private Collection<RemoteInterface> remoteInterfaces;

    private ConfigurableBeanFactory beanFactory;

    private CachingConnectionFactory connectionFactory;

    private ClusterManager clusterManager;

    private AmqpAdmin amqpAdmin;

    private List<String> topicsGenerated;

    public RPCMethodFactory(List<RemoteInterface> remoteInterfaces,
                            ConfigurableBeanFactory beanFactory,
                            CachingConnectionFactory cachingConnectionFactory,
                            ClusterManager clusterManager,
                            AmqpAdmin amqpAdmin) {
        this.remoteInterfaces = remoteInterfaces;
        this.beanFactory = beanFactory;
        this.connectionFactory = cachingConnectionFactory;
        this.clusterManager = clusterManager;
        this.amqpAdmin = amqpAdmin;
        registerMethods();
    }

    public void registerMethods() {
        List<String> registeredMethods = remoteInterfaces
                .stream()
                .flatMap(this::registerListener)
                .collect(Collectors.toList());
        topicsGenerated = Collections.unmodifiableList(registeredMethods);
        clusterManager.broadcastMethods(topicsGenerated);
    }

    @PreDestroy
    public void cleanUp() {
        topicsGenerated.stream().forEach(topic -> amqpAdmin.deleteQueue(topic));
    }

    private Stream<String> registerListener(RemoteInterface remoteInterface) {
        return asList(remoteInterface.getClass().getMethods()).stream()
                .filter(method -> method.getAnnotation(RemoteMethod.class) != null)
                .map(method -> {
                    RemoteMethod annotation = method.getAnnotation(RemoteMethod.class);
                    String queueName = annotation.name() + UUID.randomUUID();
                    registerListener(remoteInterface, queueName, method.getName(),annotation.concurrency());
                    return queueName;
                }).collect(Collectors.toList()).stream();
    }

    private void registerListener(RemoteInterface method, String queueName, String javaMethodName,int concurrency) {
        amqpAdmin.declareQueue(QueueBuilder.nonDurable(queueName).build());
        MessageListenerAdapter adapter = new MessageListenerAdapter(method, javaMethodName);
        DirectMessageListenerContainer container = new DirectMessageListenerContainer(connectionFactory);
        container.setMessageListener(adapter);
        container.setConsumersPerQueue(concurrency);
        container.setQueueNames(queueName);
        String listenerName = String.format(RPC_LISTENER_NAME_TEMPLATE, queueName);
        beanFactory.registerSingleton(listenerName, container);
    }


}
