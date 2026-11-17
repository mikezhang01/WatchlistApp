package com.cinestack.auth;

import com.cinestack.IntegrationTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthFlowTest extends IntegrationTest {
    @Test
    void registerThenLoginThenFetchOwnProfile() throws Exception {
        TestUser user = register();

        String loginToken = read(mvc.perform(send(post("/api/auth/login"), null,
                        Map.of("email", user.email(), "password", user.password())))
                .andExpect(status().isOk()))
                .get("token").asText();

        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.id().toString()))
                .andExpect(jsonPath("$.email").value(user.email()));
    }

    @Test
    void emailIsCaseInsensitive() throws Exception {
        TestUser user = register();

        mvc.perform(send(post("/api/auth/login"), null,
                        Map.of("email", user.email().toUpperCase(), "password", user.password())))
                .andExpect(status().isOk());
    }

    @Test
    void wrongPasswordIsRejected() throws Exception {
        TestUser user = register();

        mvc.perform(send(post("/api/auth/login"), null,
                        Map.of("email", user.email(), "password", "not-the-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        TestUser user = register();

        mvc.perform(send(post("/api/auth/register"), null,
                        Map.of("email", user.email(), "password", "another-password", "displayName", "Copy")))
                .andExpect(status().isConflict());
    }

    @Test
    void missingOrShortPasswordIsAValidationError() throws Exception {
        Map<String, Object> noPassword = new HashMap<>();
        noPassword.put("email", "nopass@example.com");
        noPassword.put("displayName", "No Password");

        mvc.perform(send(post("/api/auth/register"), null, noPassword))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.password").exists());

        mvc.perform(send(post("/api/auth/register"), null,
                        Map.of("email", "short@example.com", "password", "short", "displayName", "Short")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void protectedEndpointsNeedAValidToken() throws Exception {
        mvc.perform(get("/api/watchlists"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Log in to continue"));
        mvc.perform(get("/api/watchlists").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }
}
