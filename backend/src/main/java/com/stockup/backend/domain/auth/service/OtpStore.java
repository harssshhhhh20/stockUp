package com.stockup.backend.domain.auth.service;

import java.util.Optional;

public interface OtpStore {

    void save(String phone, String otp);

    Optional<String> get(String phone);

    void delete(String phone);

    boolean exists(String phone);
}
