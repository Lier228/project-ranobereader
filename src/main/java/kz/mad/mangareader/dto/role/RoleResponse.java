package kz.mad.mangareader.dto.role;

import kz.mad.mangareader.entity.Role;

public record RoleResponse(
        Long id,
        String name
) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getName());
    }
}
