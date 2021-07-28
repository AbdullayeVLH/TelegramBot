package com.code.touragentbot.services;

import com.code.touragentbot.models.Offer;
import com.code.touragentbot.models.Session;

public interface RabbitMQService {

    void sendToQueue(Session session);

    void sendToStopQueue(Session session);

    public void sendToAcceptedQueue(Offer offer);
}
