package com.code.touragentbot.models;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
@Entity
public class SessionDB {
    @Id
    private UUID sessionId;
    private Long chatId;
}
