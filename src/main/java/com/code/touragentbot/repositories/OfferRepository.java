package com.code.touragentbot.repositories;

import com.code.touragentbot.models.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, Long> {

    Offer getOfferByRequestId(UUID requestId);
}
