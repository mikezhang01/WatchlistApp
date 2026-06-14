package com.cinestack.auth;

import com.cinestack.auth.AuthDtos.AuthResponse;
import com.cinestack.auth.AuthDtos.LoginRequest;
import com.cinestack.auth.AuthDtos.RegisterRequest;
import com.cinestack.users.UserDtos.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final CurrentUser currentUser;

    public AuthController(AuthService authService, CurrentUser currentUser) {
        this.authService = authService;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    UserResponse me(Authentication authentication) {
        return UserResponse.from(currentUser.require(authentication));
    }
}

