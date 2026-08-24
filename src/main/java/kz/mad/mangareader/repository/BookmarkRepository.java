package kz.mad.mangareader.repository;

import kz.mad.mangareader.entity.Bookmark;
import kz.mad.mangareader.entity.BookmarkId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookmarkRepository extends JpaRepository<Bookmark, BookmarkId> {
    List<Bookmark> findByUserId(Long userId);
    List<Bookmark> findByUserIdAndStatus(Long userId, String status);
}
