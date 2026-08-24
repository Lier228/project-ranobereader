package kz.mad.mangareader.dto.novel;

import kz.mad.mangareader.entity.Chapter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ChapterResponse(
        Long id,
        Long novelId,
        Integer tomeNumber,
        BigDecimal chapterNumber,
        String title,
        String content,
        OffsetDateTime createdAt
) {
    public static ChapterResponse from(Chapter chapter) {
        return new ChapterResponse(
                chapter.getId(),
                chapter.getNovel().getId(),
                chapter.getTomeNumber(),
                chapter.getChapterNumber(),
                chapter.getTitle(),
                chapter.getContent(),
                chapter.getCreatedAt()
        );
    }
}
