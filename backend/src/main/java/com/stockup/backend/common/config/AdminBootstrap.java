package com.stockup.backend.common.config;

import com.stockup.backend.domain.user.entity.User;
import com.stockup.backend.domain.user.entity.enums.Role;
import com.stockup.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Grants ADMIN to the email in `stockup.admin.email` on boot, so there is a
 * supported way to create the first admin instead of hand-writing SQL.
 *
 * The user must already exist (i.e. have signed in once with that email) —
 * this only elevates, it never creates an account, so a stray config value
 * can't mint a login.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrap {

    @Value("${stockup.admin.email:}")
    private String adminEmail;

    @Bean
    public ApplicationRunner grantAdminRole(UserRepository userRepository) {
        return args -> elevate(userRepository);
    }

    @Transactional
    protected void elevate(UserRepository userRepository) {

        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }

        User user = userRepository.findByEmail(adminEmail.trim()).orElse(null);

        if (user == null) {
            log.warn(
                    "stockup.admin.email is set to '{}' but no such user exists yet. "
                            + "Sign in once with that email, then restart to grant ADMIN.",
                    adminEmail
            );
            return;
        }

        if (user.hasRole(Role.ADMIN)) {
            return;
        }

        user.addRole(Role.ADMIN);
        userRepository.save(user);

        log.info("Granted ADMIN to {}", adminEmail);
    }
}
