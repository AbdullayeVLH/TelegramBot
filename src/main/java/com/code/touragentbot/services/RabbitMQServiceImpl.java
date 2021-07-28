package com.code.touragentbot.services;


import com.code.touragentbot.configs.RabbitMQConfig;
import com.code.touragentbot.models.Offer;
import com.code.touragentbot.models.Session;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RabbitMQServiceImpl implements RabbitMQService{


    private final RabbitTemplate template;

    public RabbitMQServiceImpl(RabbitTemplate template) {
        this.template = template;
    }


    @Override
    public void sendToQueue(Session session) {
        session = session.toBuilder().sessionId(UUID.randomUUID()).build();
        template.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, session);
    }

    @Override
    public void sendToStopQueue(Session session) {
        template.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.STOP_QUEUE, session);
    }

    @Override
    public void sendToAcceptedQueue(Offer offer){
        template.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ACCEPTED_QUEUE, offer);
    }
}