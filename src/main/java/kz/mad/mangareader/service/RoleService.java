package kz.mad.mangareader.service;

import kz.mad.mangareader.dto.role.RoleRequest;
import kz.mad.mangareader.dto.role.RoleResponse;
import kz.mad.mangareader.entity.Role;
import kz.mad.mangareader.repository.RoleRepository;
import kz.mad.mangareader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleResponse::from)
                .toList();
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        String roleName = request.name().trim().toUpperCase();
        if (roleRepository.existsByName(roleName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Такая роль уже существует");
        }

        Role role = new Role(roleName);
        return RoleResponse.from(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Роль не найдена"));

        if (role.getName().equals("ROLE_USER") || role.getName().equals("ROLE_ADMIN") || role.getName().equals("ROLE_SCANLATOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Системную роль удалять нельзя");
        }

        if (userRepository.existsByRolesId(roleId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Роль назначена пользователям");
        }

        roleRepository.delete(role);
    }
}
