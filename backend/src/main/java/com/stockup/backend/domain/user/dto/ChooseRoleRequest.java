package com.stockup.backend.domain.user.dto;

import com.stockup.backend.domain.user.entity.enums.Role;
import jakarta.validation.constraints.NotNull;

/** The one-time fork: shopper or shopkeeper. */
public record ChooseRoleRequest(

        @NotNull(message = "Pick whether you're shopping or running a shop.")
        Role role

) {
}
