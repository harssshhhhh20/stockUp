package com.stockup.backend.domain.broadcast.repository;

import com.stockup.backend.domain.broadcast.entity.Broadcast;
import com.stockup.backend.domain.basket.entity.Basket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BroadcastRepository extends JpaRepository<Broadcast, UUID> {

    boolean existsByBasket(Basket basket);

    Optional<Broadcast> findByBasket(Basket basket);

}