package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Session;
import com.code.touragentbot.models.SessionDB;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SessionRepository {

    private final SessionDBRepository sessionDBRepo;

    public static final String HASH_KEY = "Session";

    private final RedisTemplate template;

    public SessionRepository(SessionDBRepository sessionDBRepo, @Qualifier("redis") RedisTemplate template) {
        this.sessionDBRepo = sessionDBRepo;
        this.template = template;
    }

    public Session save(Session session){
        template.opsForHash().put(HASH_KEY, session.getChatId(), session);
        sessionDBRepo.save(SessionDB.builder()
                .sessionId(session.getSessionId())
                .chatId(session.getChatId())
                .build());
        return session;
    }

    public Session find(Long chatId){
        return (Session) template.opsForHash().get(HASH_KEY, chatId);
    }

    public Optional<SessionDB> findBySessionId(UUID sessionId){
        return sessionDBRepo.findById(sessionId);
    }

    public String delete(Long chatId){
        template.opsForHash().delete(HASH_KEY, chatId);
        return "Your order has been removed.";
    }
}
