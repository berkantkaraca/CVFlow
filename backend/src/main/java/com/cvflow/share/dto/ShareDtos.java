package com.cvflow.share.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.Instant;
import java.util.UUID;

public final class ShareDtos {
    private ShareDtos() {}

    public record CreateShareRequest(@Min(1) @Max(365) Integer expiresInDays) {}

    public record ShareResponse(UUID id, String token, Instant expiresAt, Instant createdAt) {}
}
