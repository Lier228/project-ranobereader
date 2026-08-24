package kz.mad.mangareader.dto.novel;

import kz.mad.mangareader.entity.Genre;

public record GenreResponse(
        Long id,
        String name,
        String slug
) {
    public static GenreResponse from(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName(), genre.getSlug());
    }
}
