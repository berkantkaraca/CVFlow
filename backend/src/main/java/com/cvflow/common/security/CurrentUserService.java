package com.cvflow.common.security;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private static final UUID MOCK_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public UUID getCurrentUserId() {
        return MOCK_USER_ID;
    }
}
