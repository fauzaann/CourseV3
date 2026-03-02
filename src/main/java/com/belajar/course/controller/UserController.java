package com.belajar.course.controller;

import com.belajar.course.model.models.User;
import com.belajar.course.model.enums.Role;
import com.belajar.course.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> users = userRepository.findAll()
                .stream()
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id",       u.getId());
                    map.put("fullName", u.getFullName() != null ? u.getFullName() : "");
                    map.put("email",    u.getEmail()    != null ? u.getEmail()    : "");
                    map.put("role",     u.getRole().name());
                    map.put("enabled",  u.isEnabled());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        user.setRole(Role.valueOf(body.get("role").toUpperCase()));
        userRepository.save(user);

        Map<String, Object> map = new HashMap<>();
        map.put("id",       user.getId());
        map.put("fullName", user.getFullName() != null ? user.getFullName() : "");
        map.put("email",    user.getEmail()    != null ? user.getEmail()    : "");
        map.put("role",     user.getRole().name());
        return ResponseEntity.ok(map);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}