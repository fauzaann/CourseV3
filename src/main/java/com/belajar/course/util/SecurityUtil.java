package com.belajar.course.util;

import com.belajar.course.exception.ResourceNotFoundException;
import com.belajar.course.exception.UnauthorizedException;
import com.belajar.course.model.enums.Role;
import com.belajar.course.model.models.User;
import com.belajar.course.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        String email = getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan"));
    }

    public String getCurrentUserEmail() {
        return getAuthentication().getName();
    }

    public Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("Anda harus login");
        }

        return authentication;
    }

    public boolean hasRole(Role role) {
        return getAuthentication()
                .getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_" + role.name()));
    }

    public boolean hasAnyRole(Role... roles) {
        for (Role role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public boolean isStudent() {
        return hasRole(Role.STUDENT);
    }

    public boolean isInstructor() {
        return hasAnyRole(Role.INSTRUCTOR, Role.ADMIN);
    }

    public boolean isOwner(Long ownerId) {
        return ownerId != null && ownerId.equals(getCurrentUser().getId());
    }
}