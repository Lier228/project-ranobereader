package kz.mad.mangareader.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank
        @Size(max = 100)
        @Pattern(regexp = "^ROLE_[A-Z0-9_]+$", message = "Роль должна начинаться с ROLE_")
        String name
) {
}
