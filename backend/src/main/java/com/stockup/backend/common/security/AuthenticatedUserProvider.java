package com.stockup.backend.common.security;

import com.stockup.backend.domain.user.entity.User;

public interface AuthenticatedUserProvider {

    User getAuthenticatedUser();

}