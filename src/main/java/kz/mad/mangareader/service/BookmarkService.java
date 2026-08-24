package kz.mad.mangareader.service;

import kz.mad.mangareader.dto.novel.BookmarkRequest;
import kz.mad.mangareader.dto.novel.BookmarkResponse;
import kz.mad.mangareader.entity.Bookmark;
import kz.mad.mangareader.entity.BookmarkId;
import kz.mad.mangareader.entity.Novel;
import kz.mad.mangareader.entity.User;
import kz.mad.mangareader.repository.BookmarkRepository;
import kz.mad.mangareader.repository.NovelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkService {
    private final BookmarkRepository bookmarkRepository;
    private final NovelRepository novelRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public List<BookmarkResponse> getUserBookmarks(String email, String status) {
        User user = userService.findUserByEmail(email);
        List<Bookmark> bookmarks = (status != null && !status.trim().isEmpty())
                ? bookmarkRepository.findByUserIdAndStatus(user.getId(), status.toUpperCase())
                : bookmarkRepository.findByUserId(user.getId());

        return bookmarks.stream().map(BookmarkResponse::from).toList();
    }

    @Transactional
    public BookmarkResponse addOrUpdateBookmark(String email, BookmarkRequest request){
        User user = userService.findUserByEmail(email);
        Novel novel = novelRepository.findById(request.novelId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ранобэ не найдено"));

        BookmarkId id = new BookmarkId(user.getId(), novel.getId());
        Bookmark bookmark = bookmarkRepository.findById(id).orElseGet(() -> {
            Bookmark b = new Bookmark();
            b.setNovel(novel);
            b.setUser(user);
            return b;
        });

        bookmark.setStatus(request.status().toUpperCase());
        return BookmarkResponse.from(bookmarkRepository.save(bookmark));
    }

    @Transactional
    public void deleteBookmark(String email, Long novelId) {
        User user = userService.findUserByEmail(email);
        BookmarkId id = new BookmarkId(user.getId(), novelId);
        bookmarkRepository.deleteById(id);
    }
}
