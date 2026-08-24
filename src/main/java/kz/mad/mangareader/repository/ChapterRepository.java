package kz.mad.mangareader.repository;

import kz.mad.mangareader.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByNovelIdOrderByTomeNumberAscChapterNumberAsc(Long novelId);
}
