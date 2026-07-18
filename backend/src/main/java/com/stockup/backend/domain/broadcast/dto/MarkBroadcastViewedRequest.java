package com.stockup.backend.domain.broadcast.dto;

import java.util.UUID;

public record MarkBroadcastViewedRequest(
        UUID broadcastRecipientId
) {}
