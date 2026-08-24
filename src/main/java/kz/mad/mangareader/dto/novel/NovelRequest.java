package kz.mad.mangareader.dto.novel;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record NovelRequest(
        @NotBlank String titleRu,
        String titleEn,
        @NotBlank String slug,
        String description,
        String coverImage,
        String author,
        String status,
        Set<Long> genreIds
) {
}
