package kz.mad.mangareader.service;

import kz.mad.mangareader.dto.auth.RegisterRequest;
import kz.mad.mangareader.dto.user.UserResponse;
import kz.mad.mangareader.entity.Role;
import kz.mad.mangareader.entity.User;
import kz.mad.mangareader.repository.RoleRepository;
import kz.mad.mangareader.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Пользователь с таким email уже существует");
        }

        if (!request.password().equals(request.repeatPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пароли не совпадают");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Роль ROLE_USER не найдена"));

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEnabled(true);
        user.getRoles().add(userRole);

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String email) {
        User user = findUserByEmail(email);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse addRole(Long userId, Long roleId) {
        User user = findUserById(userId);
        Role role = findRoleById(roleId);
        user.getRoles().add(role);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse removeRole(Long userId, Long roleId, String currentAdminEmail) {
        User user = findUserById(userId);
        Role role = findRoleById(roleId);

        if (user.getEmail().equalsIgnoreCase(currentAdminEmail) && role.getName().equals("ROLE_ADMIN")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя удалить ROLE_ADMIN у текущего администратора");
        }

        user.getRoles().remove(role);
        return UserResponse.from(userRepository.save(user));
    }

    public User findUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
    }

    private Role findRoleById(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Роль не найдена"));
    }
}
