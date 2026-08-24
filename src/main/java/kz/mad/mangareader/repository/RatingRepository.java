package kz.mad.mangareader.repository;

import kz.mad.mangareader.entity.Rating;
import kz.mad.mangareader.entity.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, RatingId> {
    Optional<Rating> findByUserIdAndNovelId(Long userId, Long novelId);

    long countByNovelId(Long novelId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.novel.id = :novelId")
    Double getAverageRatingByNovelId(@Param("novelId") Long novelId);
}
