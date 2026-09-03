package com.stockup.backend.domain.user.repository;

import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.domain.user.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhone(String phone);

    /**
     * Is this number already claimed by a *different* account on the same side
     * of the marketplace? Shopkeepers and shoppers are counted separately, so
     * one person can run a shop and shop with the same number.
     */
    @Query("""
            SELECT COUNT(u) > 0 FROM User u
            WHERE u.phone = :phone
              AND u.id <> :excludingUserId
              AND ((:merchant = TRUE AND :merchantRole MEMBER OF u.roles)
                OR (:merchant = FALSE AND :merchantRole NOT MEMBER OF u.roles))
            """)
    boolean existsByPhoneInRole(
            @Param("phone") String phone,
            @Param("excludingUserId") UUID excludingUserId,
            @Param("merchant") boolean merchant,
            @Param("merchantRole") Role merchantRole);

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);
}