package com.code.touragentbot.models;


import lombok.*;
import org.hibernate.annotations.Proxy;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Proxy(lazy = false)
public class Accepted {
    @Id
    private UUID requestId;
    private String contactInfo;
    private String agentEmail;
}
