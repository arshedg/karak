package com.devs.karak.business.solution;

import com.devs.karak.rabbitmq.RemoteMethod;
import com.devs.karak.rabbitmq.RemoteInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.support.atomic.RedisAtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class KarakAlgorithms implements RemoteInterface {

    @Autowired
    private RedisAtomicInteger counter;

    @RemoteMethod(name="algo", concurrency = 5)
    public String execute(String message) {
        System.out.println("someone invoked [method one]");
        return "invocation count from one: "+counter.incrementAndGet();
    }

    @RemoteMethod(name="golbalnamespace.secondMethod")
    public String second(String message) {
        System.out.println("someone invoked method second");
        return "invocation count from second: "+counter.incrementAndGet();
    }

    @RemoteMethod(name="thirdMethod")
    public String method4(String message) {
        System.out.println("someone invoked method third");
        return "invocation count from third: "+counter.incrementAndGet();
    }

    @RemoteMethod(name="fourthMethod")
    public String method5(String message) {
        System.out.println("someone invoked method four");
        return "invocation count from five: "+counter.incrementAndGet();
    }

    @RemoteMethod(name="fifethMethod")
    public String methodRandom(String message) {
        System.out.println("someone invoked method five");
        return "invocation count from five: "+counter.incrementAndGet();
    }





}
