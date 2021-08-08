package com.code.touragentbot.models;

import lombok.*;
import org.springframework.data.redis.core.RedisHash;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@Builder(toBuilder = true)
@RedisHash("Session")
@ToString
public class Session implements Serializable {
    private UUID sessionId;
    private Long chatId;
    private String lang;

    private Map<String, String> data;

    private Action action;

    public Session() {
        this.data=new HashMap<>();
    }

    public void setData(String key, String answer) {
        data.put(key, answer);
    }
}
