package com.devs.karak.cluster;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class JobScheduler {

    private ThreadPoolTaskScheduler executor;

    private RabbitTemplate rabbitTemplate;

    private Map<String, JobExecutor> jobMap = new HashMap<>();

    public JobScheduler(CachingConnectionFactory connectionFactory) {
        this.executor = taskExecutor();
        this.rabbitTemplate = createRabbitTemplate(connectionFactory);
    }


    public void schedule(String group, String queueName) {
        JobExecutor jobExecutor = jobMap.get(group);
        if (jobExecutor == null) {
            jobExecutor = new JobExecutor(rabbitTemplate);
            jobMap.put(group, jobExecutor);
            executor.scheduleWithFixedDelay(jobExecutor, 5000);
        }
        jobExecutor.addMethod(queueName);
    }

    private RabbitTemplate createRabbitTemplate(CachingConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setReplyTimeout(-1);
        return template;
    }

    private ThreadPoolTaskScheduler taskExecutor(){
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(30);
        executor.initialize();
        return executor;
    }



}
