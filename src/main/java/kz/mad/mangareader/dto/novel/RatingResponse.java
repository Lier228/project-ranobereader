package kz.mad.mangareader.dto.novel;

public record RatingResponse(
        double averageRating,
        long totalRatings,
        Integer userRating
) {
}
