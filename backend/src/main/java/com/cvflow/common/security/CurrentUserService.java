package com.cvflow.common.security;

import com.cvflow.common.exception.UnauthenticatedException;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UUID userId)) {
            throw new UnauthenticatedException();
        }
        return userId;
    }
}
