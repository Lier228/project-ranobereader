package kz.mad.mangareader.controller;

import jakarta.validation.Valid;
import kz.mad.mangareader.dto.role.RoleRequest;
import kz.mad.mangareader.dto.role.RoleResponse;
import kz.mad.mangareader.dto.user.UserResponse;
import kz.mad.mangareader.service.RoleService;
import kz.mad.mangareader.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRoleController {

    private final RoleService roleService;
    private final UserService userService;

    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getRoles() {
        List<RoleResponse> roles = roleService.getRoles();
        return ResponseEntity.ok(roles);
    }

    @PostMapping("/roles")
    public ResponseEntity<RoleResponse> createRole(@RequestBody @Valid RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/roles/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<UserResponse> addRole(@PathVariable Long userId, @PathVariable Long roleId) {
        UserResponse response = userService.addRole(userId, roleId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/users/{userId}/roles/{roleId}")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId,
            Authentication authentication
    ) {
        UserResponse response = userService.removeRole(userId, roleId, authentication.getName());
        return ResponseEntity.ok(response);
    }
}
