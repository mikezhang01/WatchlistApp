package com.cinestack.users;

import java.util.UUID;

public final class UserDtos {
    private UserDtos() {
    }

    public record UserResponse(UUID id, String email, String displayName, UserRole role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
        }
    }
}

