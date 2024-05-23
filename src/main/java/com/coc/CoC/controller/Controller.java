package com.coc.CoC.controller;

import com.coc.CoC.models.Code;
import com.coc.CoC.models.Output;
import com.coc.CoC.service.Service;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@RestController
public class Controller {

    @Autowired
    Service service;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    String exchange;

    @Value("${rabbitmq.routing.key}")
    String routingKey;

    @Value("${rabbitmq.reply.routing.key}")
    String reply_routing_key;

    public ConcurrentHashMap<String, CompletableFuture<Output>> pendingRequests = new ConcurrentHashMap<>();

    @PostMapping(value = "/run")
    public ResponseEntity<Output> runCode(@RequestBody Code code) throws ExecutionException, InterruptedException {

        String correlate_id = UUID.randomUUID().toString();
        CompletableFuture<Output> future = new CompletableFuture<>();

        pendingRequests.put(correlate_id , future);

        rabbitTemplate.convertAndSend(exchange , routingKey , code , message1 -> {
            message1.getMessageProperties().setCorrelationId(correlate_id);
            message1.getMessageProperties().setReplyTo(reply_routing_key);
            return message1;
        });

        return ResponseEntity.ok(future.get());
    }


    @RabbitListener(queues = "${rabbitmq.reply.queue.name}")
    public void receiveReply(Message message, Output output){

        String correlation_id = message.getMessageProperties().getCorrelationId();
        CompletableFuture<Output> future = pendingRequests.get(correlation_id);
        if(future != null){
            future.complete(output);
        }
    }

}


/*

    runcode() gets the Code obj from frontend test it and return to JSON
              Handle file name collision like in java

    Lang Code : 1 -> JAVA
 */