package kz.mad.mangareader.service;

import kz.mad.mangareader.dto.novel.*;
import kz.mad.mangareader.entity.*;
import kz.mad.mangareader.mapper.ChapterMapper;
import kz.mad.mangareader.mapper.NovelMapper;
import kz.mad.mangareader.repository.ChapterRepository;
import kz.mad.mangareader.repository.GenreRepository;
import kz.mad.mangareader.repository.NovelRepository;
import kz.mad.mangareader.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NovelService {
    private final NovelRepository novelRepository;
    private final GenreRepository genreRepository;
    private final ChapterRepository chapterRepository;
    private final RatingRepository ratingRepository;
    private final UserService userService;
    private final NovelMapper novelMapper;
    private final ChapterMapper chapterMapper;
    private final SlugService slugService;

    @Transactional(readOnly = true)
    public NovelPageResponse getAllNovels(String query, Long genreId, int page, int size) {
        String search =(query != null && !query.trim().isEmpty()) ? query.trim() : null;
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by("id").descending());

        Page<Novel> pageResult;
        if(search != null && genreId != null) {
            pageResult = novelRepository.searchByQueryAndGenre(search, genreId, pageable);
        } else if (search != null) {
            pageResult = novelRepository.findByTitleRuContainingIgnoreCaseOrTitleEnContainingIgnoreCase(search, search, pageable);
        } else if(genreId != null) {
            pageResult = novelRepository.findByGenresId(genreId, pageable);
        } else{
            pageResult = novelRepository.findAll(pageable);
        }

        List<NovelResponse> content = pageResult.getContent().stream().map(this::mapToResponse).toList();

        return new NovelPageResponse(
                content,
                pageResult.getNumber(),
                pageResult.getTotalPages(),
                pageResult.getTotalElements(),
                pageResult.getSize(),
                pageResult.hasPrevious(),
                pageResult.hasNext()
        );
    }

    @Transactional(readOnly = true)
    public NovelResponse getNovelById(Long id) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ранобэ не найдено"));
        return mapToResponse(novel);
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(GenreResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ChapterResponse> getChaptersByNovelId(Long novelId) {
        return chapterRepository.findByNovelIdOrderByTomeNumberAscChapterNumberAsc(novelId)
                .stream()
                .map(ChapterResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChapterResponse getChapterById(Long chapterId) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Глава не найдена"));
        return ChapterResponse.from(chapter);
    }

    @Transactional
    public NovelResponse createNovel(NovelRequest request) {
        Novel novel = novelMapper.toEntity(request);

        String slug = request.slug();

        if (slug == null || slug.isBlank()) {
            slug = slugService.createSlug(request.titleRu());
        } else {
            slug = slugService.createSlug(slug);
        }

        novel.setSlug(slug);

        if (request.genreIds() != null && !request.genreIds().isEmpty()) {
            Set<Genre> genres = new HashSet<>(
                    genreRepository.findAllById(request.genreIds())
            );
            novel.setGenres(genres);
        }

        return novelMapper.toResponse(novelRepository.save(novel));
    }

    @Transactional
    public ChapterResponse createChapter(ChapterRequest request) {
        Novel novel = novelRepository.findById(request.novelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ранобэ не найдено"));

        Chapter chapter = chapterMapper.toEntity(request);
        chapter.setNovel(novel);
        chapter.setTomeNumber(
                request.tomeNumber() != null ? request.tomeNumber() : 1
        );

        return ChapterResponse.from(chapterRepository.save(chapter));
    }

    @Transactional
    public NovelResponse updateNovel(Long id, NovelRequest request) {
        Novel novel = novelRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Ранобэ не найдено"
                        )
                );

        novelMapper.updateEntity(request, novel);

        if (request.slug() != null && !request.slug().isBlank()) {
            novel.setSlug(slugService.createSlug(request.slug()));
        }

        if (request.genreIds() != null) {
            Set<Genre> genres = new HashSet<>(
                    genreRepository.findAllById(request.genreIds())
            );
            novel.setGenres(genres);
        } else {
            novel.getGenres().clear();
        }

        return novelMapper.toResponse(novel);
    }

    @Transactional
    public ChapterResponse updateChapter(Long chapterId, ChapterRequest request) {
        Chapter chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Глава не найдена"));

        chapter.setTomeNumber(request.tomeNumber() != null ? request.tomeNumber() : 1);
        chapter.setChapterNumber(request.chapterNumber());
        chapter.setTitle(request.title());
        chapter.setContent(request.content());

        return ChapterResponse.from(chapterRepository.save(chapter));
    }

    @Transactional
    public RatingResponse addOrUpdateRating(String email, Long novelId, RatingRequest request) {
        User user = userService.findUserByEmail(email);
        Novel novel = novelRepository.findById(novelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ранобэ не найдено"));

        RatingId id = new RatingId(user.getId(), novel.getId());
        Rating rating = ratingRepository.findById(id).orElseGet(() -> {
            Rating r = new Rating();
            r.setUser(user);
            r.setNovel(novel);
            return r;
        });

        rating.setRating(request.rating());
        ratingRepository.save(rating);

        return getRatingInfo(novelId, user.getId());
    }

    @Transactional(readOnly = true)
    public RatingResponse getRatingInfo(Long novelId, Long userId) {
        Double avg = ratingRepository.getAverageRatingByNovelId(novelId);
        long count = ratingRepository.countByNovelId(novelId);
        double average = avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;

        Integer userRating = null;
        if (userId != null) {
            Optional<Rating> r = ratingRepository.findByUserIdAndNovelId(userId, novelId);
            if (r.isPresent()) {
                userRating = r.get().getRating();
            }
        }

        return new RatingResponse(average, count, userRating);
    }

    @Transactional
    public void deleteNovel(Long id) {
        if (!novelRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ранобэ не найдено");
        }
        novelRepository.deleteById(id);
    }

    @Transactional
    public void deleteChapter(Long chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Глава не найдена");
        }
        chapterRepository.deleteById(chapterId);
    }

    private NovelResponse mapToResponse(Novel novel) {
        Double avg = ratingRepository.getAverageRatingByNovelId(novel.getId());
        long count = ratingRepository.countByNovelId(novel.getId());
        double average = avg != null ? avg : 0.0;
        return NovelResponse.from(novel, average, count);
    }
}
