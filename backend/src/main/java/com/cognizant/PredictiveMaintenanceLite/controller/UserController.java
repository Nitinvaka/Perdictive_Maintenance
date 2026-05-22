package com.cognizant.PredictiveMaintenanceLite.controller;

import com.cognizant.PredictiveMaintenanceLite.entity.User;
import com.cognizant.PredictiveMaintenanceLite.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Request body for PATCH /status
    public record StatusRequest(Boolean active) {}

    @GetMapping
    public List<User> getAll() {
        return userService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    // Toggle a user active/inactive — only admin can call this.
    // The JwtAuthFilter will immediately reject inactive users on their next request.
    @PatchMapping("/{id}/status")
    public User setStatus(@PathVariable Long id,
                          @RequestBody StatusRequest body,
                          Authentication auth) {
        User current = (User) auth.getPrincipal();
        boolean active = body.active() != null ? body.active() : true;
        return userService.setStatus(id, active, current.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        User current = (User) auth.getPrincipal();
        userService.delete(id, current.getId());
    }
}
