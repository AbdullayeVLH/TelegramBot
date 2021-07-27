package com.code.touragentbot.services;

import com.code.touragentbot.models.Session;

public interface RabbitMQService {

    void sendToQueue(Session session);
}
