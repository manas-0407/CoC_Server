package com.coc.CoC.rabbit.Listener;

import com.coc.CoC.models.Code;
import com.coc.CoC.models.Output;
import com.coc.CoC.service.Service;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @Autowired
    Service service;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    String exchange;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consumer(Message message, Code code){

        String correaltion_id = message.getMessageProperties().getCorrelationId();
        String replyTo = message.getMessageProperties().getReplyTo();

        // Process task Here

        System.err.println("Inside processing Queue");

        Output output = null;
        if(code.getLang_code() == 1){
            output = service.threadHandler(code.getCode(),code.getInput());
        }

        System.err.println(output.toString());

        assert output != null;
        rabbitTemplate.convertAndSend(exchange, replyTo, output, replyMessage -> {
            replyMessage.getMessageProperties().setCorrelationId(correaltion_id);
            return  replyMessage;
        });
    }

}
