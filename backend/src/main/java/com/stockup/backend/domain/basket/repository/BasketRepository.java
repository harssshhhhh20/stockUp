package com.stockup.backend.domain.basket.repository;

import com.stockup.backend.domain.basket.entity.Basket;
import com.stockup.backend.domain.basket.enums.BasketStatus;
import com.stockup.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BasketRepository extends JpaRepository<Basket, UUID> {

    boolean existsByCustomerAndStatus(User customer, BasketStatus status);

    Optional<Basket> findByIdAndCustomer(UUID basketId, User customer);

    List<Basket> findAllByCustomerOrderByCreatedAtDesc(User customer);
}