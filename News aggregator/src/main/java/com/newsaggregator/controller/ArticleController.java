package com.newsaggregator.controller;

import com.newsaggregator.dto.ArticleResponseDTO;
import com.newsaggregator.dto.SourceResponseDTO;
import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Article;
import com.newsaggregator.model.UserArticleReadStatus;
import com.newsaggregator.security.services.UserDetailsImpl;
import com.newsaggregator.service.ArticleService;
import com.newsaggregator.service.ReadStatusFilter;
import com.newsaggregator.repository.BookmarkRepository;
import com.newsaggregator.repository.UserArticleReadStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust CORS for production
@RestController
@RequestMapping("/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final UserArticleReadStatusRepository readStatusRepository;
    private final BookmarkRepository bookmarkRepository;

    @Autowired
    public ArticleController(ArticleService articleService,
                             UserArticleReadStatusRepository readStatusRepository,
                             BookmarkRepository bookmarkRepository) {
        this.articleService = articleService;
        this.readStatusRepository = readStatusRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    /**
     * Helper method to get User ID from Authentication object.
     * Returns null if user is not authenticated or principal is not UserDetailsImpl.
     */
    private Long getUserIdFromAuthentication(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }

    /**
     * Helper method to map an Article entity to an ArticleResponseDTO,
     * populating isRead and isBookmarked flags for a given user.
     */
    private ArticleResponseDTO mapArticleToDto(Article article, Long userId, Map<Long, Boolean> readStatusMap, Map<Long, Boolean> bookmarkStatusMap) {
        ArticleResponseDTO dto = new ArticleResponseDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setDescription(article.getDescription());
        dto.setPublishedDate(article.getPublishedDate());
        dto.setUrl(article.getUrl());
        dto.setImageUrl(article.getImageUrl());
        dto.setAuthor(article.getAuthor());
        dto.setCategory(article.getCategory());
        dto.setViewCount(article.getViewCount());

        if (article.getSource() != null) {
            // FIX for "cannot find symbol method getApiId()":
            // Using article.getSource().getName() as the apiId, assuming Source model doesn't have a specific getApiId().
            // If your Source model HAS an actual apiId field, use article.getSource().getApiId() instead.
            dto.setSource(new SourceResponseDTO(article.getSource().getId(), article.getSource().getName(), article.getSource().getName()));
        }

        // Set isRead and isBookmarked based on the provided maps
        if (userId != null) {
            dto.setIsRead(readStatusMap.getOrDefault(article.getId(), false));
            dto.setIsBookmarked(bookmarkStatusMap.getOrDefault(article.getId(), false));
        } else {
            dto.setIsRead(false); // Default to false if no user context
            dto.setIsBookmarked(false); // Default to false if no user context
        }
        return dto;
    }

    /**
     * Helper method to convert a Page of Article entities to a Page of ArticleResponseDTOs,
     * including user-specific status flags.
     */
    private Page<ArticleResponseDTO> convertArticlePageToDtoPage(Page<Article> articlePage, Long userId) {
        if (userId == null) {
            // If no user, map without checking read/bookmark status
            return articlePage.map(article -> mapArticleToDto(article, null, Collections.emptyMap(), Collections.emptyMap()));
        }

        List<Long> articleIds = articlePage.getContent().stream()
                .map(Article::getId)
                .collect(Collectors.toList());

        // Fetch read statuses in bulk
        Map<Long, Boolean> readStatusMap = readStatusRepository.findByUserIdAndArticleIdIn(userId, articleIds)
                .stream()
                .collect(Collectors.toMap(
                        uars -> uars.getArticle().getId(),
                        // FIX for "cannot find symbol method getIsRead()":
                        // UserArticleReadStatus uses `boolean isRead`, so its getter is `isRead()`.
                        UserArticleReadStatus::isRead
                ));

        // Fetch bookmark statuses in bulk
        Map<Long, Boolean> bookmarkStatusMap = bookmarkRepository.findByUserIdAndArticleIdIn(userId, articleIds)
                .stream()
                .collect(Collectors.toMap(
                        bookmark -> bookmark.getArticle().getId(),
                        bookmark -> true // If a bookmark exists, it's bookmarked
                ));

        return articlePage.map(article -> mapArticleToDto(article, userId, readStatusMap, bookmarkStatusMap));
    }


    /**
     * Retrieves a paginated list of the latest news articles.
     * Accessible by anyone (unauthenticated users can get articles without read/bookmark status).
     *
     * @param authentication The Spring Security Authentication object (optional).
     * @param page The page number (0-indexed, default 0).
     * @param size The number of articles per page (default 10).
     * @param readStatus Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return ResponseEntity with a Page of ArticleResponseDTO objects.
     */
    @GetMapping("/latest")
    public ResponseEntity<Page<ArticleResponseDTO>> getLatestArticles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReadStatusFilter readStatus) {

        Long userId = getUserIdFromAuthentication(authentication);
        Page<Article> articles = articleService.getLatestArticles(userId, page, size, readStatus);
        return ResponseEntity.ok(convertArticlePageToDtoPage(articles, userId));
    }

    /**
     * Retrieves a paginated list of top headlines.
     * Accessible by anyone (unauthenticated users).
     *
     * @param authentication The Spring Security Authentication object (optional).
     * @param page The page number (0-indexed, default 0).
     * @param size The number of articles per page (default 10).
     * @param readStatus Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return ResponseEntity with a Page of ArticleResponseDTO objects.
     */
    @GetMapping("/top-headlines")
    public ResponseEntity<Page<ArticleResponseDTO>> getTopHeadlines(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReadStatusFilter readStatus) {

        Long userId = getUserIdFromAuthentication(authentication);
        Page<Article> articles = articleService.getTopHeadlines(userId, page, size, readStatus);
        return ResponseEntity.ok(convertArticlePageToDtoPage(articles, userId));
    }

    /**
     * Retrieves a paginated list of most viewed articles.
     * Accessible by anyone (unauthenticated users).
     *
     * @param authentication The Spring Security Authentication object (optional).
     * @param page The page number (0-indexed, default 0).
     * @param size The number of articles per page (default 10).
     * @param readStatus Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return ResponseEntity with a Page of ArticleResponseDTO objects.
     */
    @GetMapping("/most-viewed")
    public ResponseEntity<Page<ArticleResponseDTO>> getMostViewedArticles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReadStatusFilter readStatus) {

        Long userId = getUserIdFromAuthentication(authentication);
        Page<Article> articles = articleService.getMostViewedArticles(userId, page, size, readStatus);
        return ResponseEntity.ok(convertArticlePageToDtoPage(articles, userId));
    }

    /**
     * Searches for articles based on a keyword and/or category.
     * Accessible by anyone (unauthenticated users).
     *
     * @param authentication The Spring Security Authentication object (optional).
     * @param keyword Optional: Keyword to search in article titles or descriptions.
     * @param category Optional: Category to filter articles by.
     * @param page The page number (0-indexed, default 0).
     * @param size The number of articles per page (default 10).
     * @param readStatus Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return ResponseEntity with a Page of ArticleResponseDTO objects.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ArticleResponseDTO>> searchArticles(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReadStatusFilter readStatus) {

        Long userId = getUserIdFromAuthentication(authentication);
        Page<Article> articles = articleService.searchArticles(userId, keyword, category, page, size, readStatus);
        return ResponseEntity.ok(convertArticlePageToDtoPage(articles, userId));
    }

    /**
     * Retrieves a personalized news feed for the authenticated user.
     * Requires user authentication (ROLE_USER or higher).
     *
     * @param authentication The Spring Security Authentication object, providing current user details.
     * @param page The page number (0-indexed, default 0).
     * @param size The number of articles per page (default 10).
     * @param readStatus Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return ResponseEntity with a Page of ArticleResponseDTO objects.
     */
    @GetMapping("/personalized")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<ArticleResponseDTO>> getPersonalizedArticles(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ReadStatusFilter readStatus) {

        Long userId = getUserIdFromAuthentication(authentication); // This will not be null due to @PreAuthorize
        Page<Article> articles = articleService.getPersonalizedArticles(userId, page, size, readStatus);
        return ResponseEntity.ok(convertArticlePageToDtoPage(articles, userId));
    }

    /**
     * Retrieves a single article by its ID.
     * Accessible by anyone. Increments view count and includes user-specific read/bookmark status.
     *
     * @param id The ID of the article.
     * @param authentication The Spring Security Authentication object (optional).
     * @return ResponseEntity with the ArticleResponseDTO object.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ArticleResponseDTO> getArticleById(
            @PathVariable Long id,
            Authentication authentication) {

        Long userId = getUserIdFromAuthentication(authentication);
        try {
            Article article = articleService.getArticleById(id, userId);

            // Fetch single article's read and bookmark status if a user is authenticated
            Map<Long, Boolean> readStatusMap = Collections.emptyMap();
            Map<Long, Boolean> bookmarkStatusMap = Collections.emptyMap();

            if (userId != null) {
                // Bulk fetch for a single item for consistency with Page mapping
                readStatusMap = readStatusRepository.findByUserIdAndArticleIdIn(userId, Collections.singletonList(id))
                        .stream()
                        .collect(Collectors.toMap(
                                uars -> uars.getArticle().getId(),
                                // UserArticleReadStatus uses `boolean isRead`, so its getter is `isRead()`.
                                UserArticleReadStatus::isRead
                        ));

                bookmarkStatusMap = bookmarkRepository.findByUserIdAndArticleIdIn(userId, Collections.singletonList(id))
                        .stream()
                        .collect(Collectors.toMap(
                                bookmark -> bookmark.getArticle().getId(),
                                bookmark -> true
                        ));
            }

            ArticleResponseDTO articleDTO = mapArticleToDto(article, userId, readStatusMap, bookmarkStatusMap);
            return ResponseEntity.ok(articleDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * NEW ENDPOINT: Toggles the read status for a specific article for the authenticated user.
     * This endpoint will handle marking an article as read or unread.
     * It addresses the 405 error you were getting for /articles/{articleId}/read-status by changing to PUT.
     *
     * @param articleId The ID of the article to mark.
     * @param markAsRead A boolean flag: true to mark as read, false to mark as unread.
     * @param authentication The Spring Security Authentication object.
     * @return ResponseEntity indicating success or failure.
     */
    @PutMapping("/{articleId}/read-status") // CHANGED FROM @PostMapping TO @PutMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Void> toggleReadStatus(@PathVariable Long articleId,
                                                 @RequestParam(defaultValue = "true") boolean markAsRead, // Default to true if not provided
                                                 Authentication authentication) {
        Long userId = getUserIdFromAuthentication(authentication);
        if (userId == null) {
            // This case should ideally be caught by @PreAuthorize, but good for defensive programming
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            articleService.toggleReadStatus(userId, articleId, markAsRead);
            return ResponseEntity.ok().build(); // 200 OK for successful operation
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Log the exception for debugging on the server side
            System.err.println("Error toggling read status for user " + userId + ", article " + articleId + ": " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}