package com.PredictiveMaintenanceLite.controller;

import com.PredictiveMaintenanceLite.entity.User;
import com.PredictiveMaintenanceLite.enums.Role;
import com.PredictiveMaintenanceLite.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    // ---- Request & Response records ----

    public record LoginRequest(String username, String password) {}

    // Returned by POST /login
    public record LoginResponse(String token, String tokenType,
                                String username, String email, Role role) {}

    // Returned by GET /me
    public record MeResponse(Long id, String username,
                             String email, Role role, boolean active) {}

    // ---- Endpoints ----

    // Returns 200 (not 201) — we're authenticating, not creating a resource
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        User user = (User) authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()))
                .getPrincipal();

        return new LoginResponse(
                jwtUtil.generateToken(user.getUsername()), "Bearer",
                user.getUsername(), user.getEmail(), user.getRole()
        );
    }

    // Returns the currently logged-in user's profile — reads from the JWT, no DB call needed
    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        User user = (User) auth.getPrincipal();
        return new MeResponse(user.getId(), user.getUsername(),
                user.getEmail(), user.getRole(), user.isActive());
    }
}
