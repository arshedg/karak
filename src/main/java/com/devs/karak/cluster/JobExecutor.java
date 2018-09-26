package com.devs.karak.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class JobExecutor implements Runnable {
    private List<String> methods;
    private RabbitTemplate template;
    private Random random = new Random();

    public JobExecutor(RabbitTemplate template) {
        methods = new ArrayList<>();
        this.template = template;
    }

    public void addMethod(String method) {
        methods.add(method);
    }

    public void run() {
        if (methods.size() == 0) {
            return;
        }
        random.ints(5, 0, methods.size())
                .forEach(index -> {
                    execute(methods.get(index));
                });
    }

    private void execute(String queueName) {
        try {
            Object recieved = template.convertSendAndReceive(queueName, "message");
            System.out.println("Received" + recieved);
        } catch (IllegalStateException exception) {
            //todo cancel the job. The remote application is probabyl dead
        }
    }


}
