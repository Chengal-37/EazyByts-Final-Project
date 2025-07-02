package com.newsaggregator.service;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Article;
import com.newsaggregator.model.Bookmark;
import com.newsaggregator.model.User;
import com.newsaggregator.repository.ArticleRepository;
import com.newsaggregator.repository.BookmarkRepository;
import com.newsaggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class BookmarkService {

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * Adds a bookmark for a user to an article.
     * @param userId The ID of the user.
     * @param articleId The ID of the article to bookmark.
     * @return The created Bookmark object.
     * @throws ResourceNotFoundException if user or article is not found.
     * @throws IllegalArgumentException if the article is already bookmarked by the user.
     */
    public Bookmark addBookmark(Long userId, Long articleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        // Check if already bookmarked
        if (bookmarkRepository.findByUserAndArticle(user, article).isPresent()) {
            throw new IllegalArgumentException("Article already bookmarked by this user.");
        }

        Bookmark bookmark = new Bookmark(user, article);
        bookmark.setBookmarkedDate(LocalDateTime.now()); // Ensure date is set upon creation
        return bookmarkRepository.save(bookmark);
    }

    /**
     * Retrieves all bookmarks for a specific user with pagination.
     * @param userId The ID of the user.
     * @param page The page number (0-indexed).
     * @param size The number of bookmarks per page.
     * @return A Page of Bookmark objects.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public Page<Bookmark> getUserBookmarks(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookmarkedDate").descending());
        return bookmarkRepository.findByUserOrderByBookmarkedDateDesc(user, pageable);
    }

    /**
     * Deletes a specific bookmark for a user.
     * @param userId The ID of the user.
     * @param articleId The ID of the article to unbookmark.
     * @throws ResourceNotFoundException if user, article, or bookmark is not found.
     */
    public void deleteBookmark(Long userId, Long articleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        // Find the specific bookmark to delete
        Bookmark bookmark = bookmarkRepository.findByUserAndArticle(user, article)
                .orElseThrow(() -> new ResourceNotFoundException("Bookmark not found for this user and article."));

        bookmarkRepository.delete(bookmark);
    }

    /**
     * Checks if a user has bookmarked a specific article.
     * @param userId The ID of the user.
     * @param articleId The ID of the article.
     * @return true if bookmarked, false otherwise.
     */
    public boolean isArticleBookmarkedByUser(Long userId, Long articleId) {
        return userRepository.findById(userId).isPresent() &&
               articleRepository.findById(articleId).isPresent() &&
               bookmarkRepository.findByUserAndArticle(
                   userRepository.getReferenceById(userId), // getReferenceById avoids fetching the full entity if not needed
                   articleRepository.getReferenceById(articleId)
               ).isPresent();
    }
}
