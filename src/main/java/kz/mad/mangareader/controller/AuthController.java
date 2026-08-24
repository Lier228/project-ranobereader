package kz.mad.mangareader.controller;

import jakarta.validation.Valid;
import kz.mad.mangareader.dto.auth.LoginRequest;
import kz.mad.mangareader.dto.auth.RegisterRequest;
import kz.mad.mangareader.dto.auth.TokenResponse;
import kz.mad.mangareader.dto.user.UserResponse;
import kz.mad.mangareader.service.JwtService;
import kz.mad.mangareader.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    @Value("${app.jwt.ttl-seconds}")
    private long ttlSeconds;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email().trim().toLowerCase(),
                            request.password()
                    )
            );

            String token = jwtService.generateToken(authentication);
            TokenResponse response = new TokenResponse(token, "Bearer", ttlSeconds);

            return ResponseEntity.ok(response);
        } catch (AuthenticationException exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Неправильный email или пароль");
        }
    }
}
