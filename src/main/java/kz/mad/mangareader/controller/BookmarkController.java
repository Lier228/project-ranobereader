package kz.mad.mangareader.controller;

import jakarta.validation.Valid;
import kz.mad.mangareader.dto.novel.BookmarkRequest;
import kz.mad.mangareader.dto.novel.BookmarkResponse;
import kz.mad.mangareader.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @GetMapping
    public ResponseEntity<List<BookmarkResponse>> getUserBookmarks(
            Authentication authentication,
            @RequestParam(required = false) String status
    ) {
        return ResponseEntity.ok(bookmarkService.getUserBookmarks(authentication.getName(), status));
    }

    @PostMapping
    public ResponseEntity<BookmarkResponse> addOrUpdateBookmark(
            Authentication authentication,
            @RequestBody @Valid BookmarkRequest request
    ) {
        return ResponseEntity.ok(bookmarkService.addOrUpdateBookmark(authentication.getName(), request));
    }

    @DeleteMapping("/{novelId}")
    public ResponseEntity<Void> deleteBookmark(
            Authentication authentication,
            @PathVariable Long novelId
    ) {
        bookmarkService.deleteBookmark(authentication.getName(), novelId);
        return ResponseEntity.noContent().build();
    }
}
