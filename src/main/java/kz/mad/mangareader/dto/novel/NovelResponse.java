package kz.mad.mangareader.dto.novel;

import kz.mad.mangareader.entity.Novel;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record NovelResponse(
        Long id,
        String titleRu,
        String titleEn,
        String slug,
        String description,
        String coverImage,
        String author,
        String status,
        double averageRating,
        long totalRatings,
        OffsetDateTime createdAt,
        Set<GenreResponse> genres
) {
    public static NovelResponse from(Novel novel) { return from(novel, 0.0, 0);}

    public static NovelResponse from(Novel novel,double averageRating,long totalRatings){
        Set<GenreResponse> genreResponses = novel.getGenres().stream().map(GenreResponse::from).collect(Collectors.toSet());

        return new NovelResponse(
                novel.getId(),
                novel.getTitleRu(),
                novel.getTitleEn(),
                novel.getSlug(),
                novel.getDescription(),
                novel.getCoverImage(),
                novel.getAuthor(),
                novel.getStatus(),
                Math.round(averageRating * 10.0) / 10.0,
                totalRatings,
                novel.getCreatedAt(),
                genreResponses
        );
    }
}
