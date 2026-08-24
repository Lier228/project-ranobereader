package kz.mad.mangareader.repository;

import kz.mad.mangareader.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface NovelRepository extends JpaRepository<Novel, Long> {
    Optional<Novel> findBySlug(String slug);

    Page<Novel> findByGenresId(Long genreId, Pageable pageable);

    Page<Novel> findByTitleRuContainingIgnoreCaseOrTitleEnContainingIgnoreCase(String titleRu, String titleEn, Pageable pageable);

    @Query("Select distinct n from Novel n join n.genres g " +
            "where g.id = :genreId " +
            "and (LOWER(n.titleRu) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "or LOWER(n.titleEn) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Novel> searchByQueryAndGenre(String query, Long genreId, Pageable pageable);
}
