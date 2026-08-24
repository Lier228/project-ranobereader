package kz.mad.mangareader.dto.novel;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookmarkRequest(
        @NotNull Long novelId,
        @NotBlank String status // READING, PLANNED, COMPLETED, DROPPED
) {
}
