package com.code.touragentbot.repositories;

import com.code.touragentbot.models.SessionDB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SessionDBRepository extends JpaRepository<SessionDB, UUID> {
}
