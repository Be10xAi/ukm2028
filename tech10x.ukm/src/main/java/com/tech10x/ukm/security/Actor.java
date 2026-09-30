package com.tech10x.ukm.security;

import org.springframework.security.core.Authentication;

/**
 * Who is making the request, taken ONLY from the authenticated JWT identity - never from
 * the request body or path. Services receive this instead of trusting caller-supplied ids.
 */
public record Actor(String userId, boolean admin) {

    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    public static Actor of(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(a -> ADMIN_AUTHORITY.equals(a.getAuthority()));
        return new Actor(authentication.getName(), admin);
    }
}
