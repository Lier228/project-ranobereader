package kz.mad.mangareader.dto.user;

import kz.mad.mangareader.entity.Role;
import kz.mad.mangareader.entity.User;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        boolean enabled,
        OffsetDateTime createdAt,
        Set<String> roles
) {
    public static UserResponse from(User user) {
        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.isEnabled(),
                user.getCreatedAt(),
                roleNames
        );
    }
}
