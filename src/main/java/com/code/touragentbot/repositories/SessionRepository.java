package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Session;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {

    public static final String HASH_KEY = "Session";

    private final RedisTemplate template;

    public SessionRepository(@Qualifier("redis") RedisTemplate template) {
        this.template = template;
    }

    public Session save(Session session){
        template.opsForHash().put(HASH_KEY, session.getChatId(), session);
        return session;
    }

    public Session find(Long chtId){
        return (Session) template.opsForHash().get(HASH_KEY, chtId);
    }

    public String delete(Long chatId){
        template.opsForHash().delete(HASH_KEY, chatId);
        return "Your order has been removed.";
    }
}
