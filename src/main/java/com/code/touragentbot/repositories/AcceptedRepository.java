package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Accepted;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcceptedRepository extends JpaRepository<Accepted, UUID> {
}
