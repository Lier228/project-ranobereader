package kz.mad.mangareader.dto.novel;

import kz.mad.mangareader.entity.Bookmark;

public record BookmarkResponse(
        Long novelId,
        String status,
        NovelResponse novel
) {
    public static BookmarkResponse from(Bookmark bookmark) {
        return new BookmarkResponse(
                bookmark.getNovel().getId(),
                bookmark.getStatus(),
                NovelResponse.from(bookmark.getNovel())
        );
    }
}
