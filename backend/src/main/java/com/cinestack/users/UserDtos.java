package com.cinestack.users;

import java.util.UUID;

public final class UserDtos {
    private UserDtos() {
    }

    /** The signed-in user's own account. Never return this for someone else. */
    public record UserResponse(UUID id, String email, String displayName, UserRole role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getRole());
        }
    }

    /** What anyone may see about a user. */
    public record PublicUserResponse(UUID id, String displayName) {
        public static PublicUserResponse from(User user) {
            return new PublicUserResponse(user.getId(), user.getDisplayName());
        }
    }
}

