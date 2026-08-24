package kz.mad.mangareader.dto.novel;

import java.util.List;

public record NovelPageResponse(
        List<NovelResponse> content,
        int currentPage,
        int totalPages,
        long totalElements,
        int pageSize,
        boolean hasPrevious,
        boolean hasNext
) {
}
