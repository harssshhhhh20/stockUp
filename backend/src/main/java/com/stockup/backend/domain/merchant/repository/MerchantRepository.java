package com.stockup.backend.domain.merchant.repository;

import com.stockup.backend.domain.merchant.entity.Merchant;
import com.stockup.backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> {

    Optional<Merchant> findByUser(User user);

    boolean existsByUser(User user);

}