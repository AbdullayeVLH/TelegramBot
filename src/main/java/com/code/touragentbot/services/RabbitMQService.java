package com.code.touragentbot.services;

import com.code.touragentbot.models.Accepted;
import com.code.touragentbot.models.Session;

public interface RabbitMQService {

    void sendToQueue(Session session);

    void sendToStopQueue(Session session);

    void sendToAcceptedQueue(Accepted offer);
}
