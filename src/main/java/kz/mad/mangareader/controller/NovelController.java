package kz.mad.mangareader.controller;

import jakarta.validation.Valid;
import kz.mad.mangareader.dto.novel.*;
import kz.mad.mangareader.service.NovelService;
import kz.mad.mangareader.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/novels")
@RequiredArgsConstructor
public class NovelController {
    private final NovelService novelService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<NovelPageResponse> getAllNovels(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long genreId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size
    ) {
        return ResponseEntity.ok(novelService.getAllNovels(query, genreId, page, size));
    }

    @GetMapping("/genres")
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        return ResponseEntity.ok(novelService.getAllGenres());
    }

    @GetMapping("/{id}")
    public ResponseEntity<NovelResponse> getNovelByUd(@PathVariable Long id) {
        return ResponseEntity.ok(novelService.getNovelById(id));
    }

    @GetMapping("/{id}/chapters")
    public ResponseEntity<List<ChapterResponse>> getChaptersByNovelId(@PathVariable Long id) {
        return ResponseEntity.ok(novelService.getChaptersByNovelId(id));
    }

    @GetMapping("/chapters/{chapterId}")
    public ResponseEntity<ChapterResponse> getChapterById(@PathVariable Long chapterId) {
        return ResponseEntity.ok(novelService.getChapterById(chapterId));
    }

    @GetMapping("/{id}/rating")
    public ResponseEntity<RatingResponse> getRating(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = null;
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            userId = userService.findUserByEmail(authentication.getName()).getId();
        }
        return ResponseEntity.ok(novelService.getRatingInfo(id, userId));
    }

    @PostMapping("/{id}/rating")
    public ResponseEntity<RatingResponse> rateNovel(
            @PathVariable Long id,
            @RequestBody @Valid RatingRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(novelService.addOrUpdateRating(authentication.getName(), id, request));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSLATOR')")
    public ResponseEntity<NovelResponse> createNovel(@RequestBody @Valid NovelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(novelService.createNovel(request));
    }

    @PostMapping("/chapters")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSLATOR')")
    public ResponseEntity<ChapterResponse> createChapter(@RequestBody @Valid ChapterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(novelService.createChapter(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSLATOR')")
    public ResponseEntity<NovelResponse> updateNovel(@PathVariable Long id, @RequestBody @Valid NovelRequest request) {
        return ResponseEntity.ok(novelService.updateNovel(id, request));
    }

    @PutMapping("/chapters/{chapterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSLATOR')")
    public ResponseEntity<ChapterResponse> updateChapter(@PathVariable Long chapterId, @RequestBody @Valid ChapterRequest request) {
        return ResponseEntity.ok(novelService.updateChapter(chapterId, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteNovel(@PathVariable Long id) {
        novelService.deleteNovel(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/chapters/{chapterId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRANSLATOR')")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long chapterId) {
        novelService.deleteChapter(chapterId);
        return ResponseEntity.noContent().build();
    }
}
