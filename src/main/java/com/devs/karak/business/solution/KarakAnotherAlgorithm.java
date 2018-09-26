package com.devs.karak.business.solution;

import com.devs.karak.rabbitmq.RemoteInterface;
import com.devs.karak.rabbitmq.RemoteMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class KarakAnotherAlgorithm implements RemoteInterface {

    @Autowired
    private RedisAtomicInteger counter;

    @RemoteMethod(name="sixth")
    public String execute(String message) {
        System.out.println("someone invoked method algo");
        return "invocation count from sixth: "+counter.incrementAndGet();
    }

    @RemoteMethod(name="seventh.global")
    public String second(String message) {
        System.out.println("someone invoked method seventh");
        return "invocation count from seventh: "+counter.incrementAndGet();
    }

    @RemoteMethod(name="eigth")
    public String method4(String message) {
        System.out.println("someone invoked method eigth");
        return "invocation count from eigth: "+counter.incrementAndGet();
    }

    @RemoteMethod(name="nineth")
    public String method5(String message) {
        System.out.println("someone invoked method nineth");
        return "invocation count from nineth: "+counter.incrementAndGet();
    }


    @RemoteMethod(name="tenth")
    public String method7(String message) {
        System.out.println("someone invoked method tenth");
        return "invocation count from tenth: "+counter.incrementAndGet();
    }
}
