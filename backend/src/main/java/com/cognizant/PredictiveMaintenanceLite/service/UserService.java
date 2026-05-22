package com.cognizant.PredictiveMaintenanceLite.service;

import com.cognizant.PredictiveMaintenanceLite.entity.User;
import com.cognizant.PredictiveMaintenanceLite.enums.Role;
import com.cognizant.PredictiveMaintenanceLite.exception.BusinessException;
import com.cognizant.PredictiveMaintenanceLite.exception.ResourceNotFoundException;
import com.cognizant.PredictiveMaintenanceLite.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User create(User incoming) {
        if (userRepository.existsByUsername(incoming.getUsername())) {
            throw new BusinessException("Username already taken");
        }
        if (userRepository.existsByEmail(incoming.getEmail())) {
            throw new BusinessException("Email already registered");
        }
        incoming.setId(null);
        incoming.setPassword(passwordEncoder.encode(incoming.getPassword()));
        if (incoming.getRole() == null) incoming.setRole(Role.ROLE_TECHNICIAN);
        return userRepository.save(incoming);
    }

    /**
     * Activate or deactivate a user.
     * Once deactivated, the JwtAuthFilter rejects their token immediately —
     * they can't perform any action even if they're already logged in.
     */
    @Transactional
    public User setStatus(Long id, boolean active, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessException("You cannot change your own active status");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setActive(active);
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new BusinessException("You cannot delete your own account");
        }
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User", id);
        }
        userRepository.deleteById(id);
    }
}
