package com.code.touragentbot.services;


import com.code.touragentbot.configs.RabbitMQConfig;
import com.code.touragentbot.models.Session;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQServiceImpl implements RabbitMQService{


    private RabbitTemplate template;

    public RabbitMQServiceImpl(RabbitTemplate template) {
        this.template = template;
    }


    @Override
    public void sendToQueue(Session session) {
        template.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, session);
    }
}
