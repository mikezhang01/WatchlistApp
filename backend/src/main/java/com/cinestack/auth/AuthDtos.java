package com.cinestack.auth;

import com.cinestack.users.UserDtos.UserResponse;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(
            @Email @NotBlank String email,
            @Size(min = 8) String password,
            @NotBlank @Size(max = 120) String displayName
    ) {
    }

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}

