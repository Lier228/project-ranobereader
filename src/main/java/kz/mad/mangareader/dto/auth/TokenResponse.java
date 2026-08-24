package kz.mad.mangareader.dto.auth;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
