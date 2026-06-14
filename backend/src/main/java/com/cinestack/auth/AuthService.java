package com.cinestack.auth;

import com.cinestack.auth.AuthDtos.AuthResponse;
import com.cinestack.auth.AuthDtos.LoginRequest;
import com.cinestack.auth.AuthDtos.RegisterRequest;
import com.cinestack.common.ApiException;
import com.cinestack.users.User;
import com.cinestack.users.UserDtos.UserResponse;
import com.cinestack.users.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = users.save(new User(email, passwordEncoder.encode(request.password()), request.displayName()));
        return new AuthResponse(jwtService.issueToken(user), UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return new AuthResponse(jwtService.issueToken(user), UserResponse.from(user));
    }
}

