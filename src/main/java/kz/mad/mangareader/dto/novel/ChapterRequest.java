package kz.mad.mangareader.dto.novel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ChapterRequest(
        @NotNull Long novelId,
        Integer tomeNumber,
        @NotNull BigDecimal chapterNumber,
        String title,
        @NotBlank String content
) {
}
