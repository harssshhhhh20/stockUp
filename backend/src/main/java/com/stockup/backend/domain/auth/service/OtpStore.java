package com.stockup.backend.domain.auth.service;

import java.util.Optional;

public interface OtpStore {

    void save(String email, String otp);

    Optional<String> get(String email);

    void delete(String email);

    boolean exists(String email);
}
