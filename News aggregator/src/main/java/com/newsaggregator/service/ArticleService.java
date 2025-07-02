package com.newsaggregator.service;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Article;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserArticleReadStatus;
import com.newsaggregator.model.UserArticleReadStatusId;
import com.newsaggregator.model.UserPreference;
import com.newsaggregator.repository.ArticleRepository;
import com.newsaggregator.repository.BookmarkRepository;
import com.newsaggregator.repository.UserArticleReadStatusRepository;
import com.newsaggregator.repository.UserRepository;
import com.newsaggregator.repository.UserPreferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;// Import Collections for singletonList (still used for specific filters)
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional // Apply @Transactional at the class level for all public methods
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserArticleReadStatusRepository readStatusRepository;
    private final BookmarkRepository bookmarkRepository; // Injected but not used in provided methods

    @Autowired
    public ArticleService(ArticleRepository articleRepository,
                          UserRepository userRepository,
                          UserPreferenceRepository userPreferenceRepository,
                          UserArticleReadStatusRepository readStatusRepository,
                          BookmarkRepository bookmarkRepository) {
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.userPreferenceRepository = userPreferenceRepository;
        this.readStatusRepository = readStatusRepository;
        this.bookmarkRepository = bookmarkRepository;
    }

    /**
     * Finds a paginated list of the latest news articles, with optional user-specific read status filtering.
     * @param userId Optional: The ID of the authenticated user.
     * @param page The page number (0-indexed).
     * @param size The number of articles per page.
     * @param readStatusFilter Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return A Page of Article objects.
     */
    public Page<Article> getLatestArticles(Long userId, int page, int size, ReadStatusFilter readStatusFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending());

        Boolean isReadFilter = null;
        if (readStatusFilter == ReadStatusFilter.READ) {
            isReadFilter = true;
        } else if (readStatusFilter == ReadStatusFilter.UNREAD) {
            isReadFilter = false;
        }

        return articleRepository.findLatestArticlesWithReadStatusFilter(userId, isReadFilter, pageable);
    }

    /**
     * Finds a paginated list of top headlines, with optional user-specific read status filtering.
     * @param userId Optional: The ID of the authenticated user.
     * @param page The page number (0-indexed).
     * @param size The number of articles per page.
     * @param readStatusFilter Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return A Page of Article objects.
     */
    public Page<Article> getTopHeadlines(Long userId, int page, int size, ReadStatusFilter readStatusFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending()); // Often top headlines are by popularity, but "publishedDate" is used here. Adjust as needed.

        Boolean isReadFilter = null;
        if (readStatusFilter == ReadStatusFilter.READ) {
            isReadFilter = true;
        } else if (readStatusFilter == ReadStatusFilter.UNREAD) {
            isReadFilter = false;
        }

        return articleRepository.findTopArticlesWithReadStatusFilter(userId, isReadFilter, pageable);
    }

    /**
     * Finds a paginated list of most viewed articles, with optional user-specific read status filtering.
     *
     * @param userId Optional: The ID of the authenticated user.
     * @param page The page number (0-indexed).
     * @param size The number of articles per page.
     * @param readStatusFilter Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return A Page of Article objects sorted by view count in descending order.
     */
    public Page<Article> getMostViewedArticles(Long userId, int page, int size, ReadStatusFilter readStatusFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("viewCount").descending());

        Boolean isReadFilter = null;
        if (readStatusFilter == ReadStatusFilter.READ) {
            isReadFilter = true;
        } else if (readStatusFilter == ReadStatusFilter.UNREAD) {
            isReadFilter = false;
        }

        return articleRepository.findMostViewedArticlesWithReadStatusFilter(userId, isReadFilter, pageable);
    }


    /**
     * Searches for articles based on a keyword and/or category, with pagination and user-specific status filtering.
     * @param userId Optional: The ID of the authenticated user.
     * @param keyword The keyword to search in title or description.
     * @param category The category to filter by.
     * @param page The page number (0-indexed).
     * @param size The number of articles per page.
     * @param readStatusFilter Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return A Page of Article objects.
     */
    public Page<Article> searchArticles(Long userId, String keyword, String category, int page, int size, ReadStatusFilter readStatusFilter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending());

        Boolean isReadFilter = null;
        if (readStatusFilter == ReadStatusFilter.READ) {
            isReadFilter = true;
        } else if (readStatusFilter == ReadStatusFilter.UNREAD) {
            isReadFilter = false;
        }

        return articleRepository.searchArticlesWithReadStatusFilter(userId, keyword, category, isReadFilter, pageable);
    }

    /**
     * Finds personalized news articles for a given user based on their preferences, with user-specific status filtering.
     * This method fetches user preferences and then queries articles matching those preferences.
     * @param userId The ID of the user.
     * @param page The page number (0-indexed).
     * @param size The number of articles per page.
     * @param readStatusFilter Optional: Filter articles by read status (ALL, READ, UNREAD).
     * @return A Page of Article objects.
     * @throws ResourceNotFoundException if the user is not found.
     */
    public Page<Article> getPersonalizedArticles(Long userId, int page, int size, ReadStatusFilter readStatusFilter) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<UserPreference> preferences = userPreferenceRepository.findByUser(user);

        List<String> preferredCategories = preferences.stream()
                .filter(p -> "CATEGORY".equalsIgnoreCase(p.getPreferenceType()))
                .map(UserPreference::getPreferenceValue)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<String> preferredKeywords = preferences.stream()
                .filter(p -> "KEYWORD".equalsIgnoreCase(p.getPreferenceType()))
                .map(UserPreference::getPreferenceValue)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<String> preferredSourceNames = preferences.stream()
                .filter(p -> "SOURCE".equalsIgnoreCase(p.getPreferenceType()))
                .map(UserPreference::getPreferenceValue)
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedDate").descending());

        // --- IMPORTANT CHANGE HERE ---
        // If a preference list is empty, we now pass 'null' to the repository.
        // The repository's JPQL query handles 'IS NULL' to effectively skip that filter.
        List<String> categoriesToQuery = preferredCategories.isEmpty()
                                             ? null // Pass null to skip category filtering if no preferences
                                             : preferredCategories;

        List<String> sourceNamesToQuery = preferredSourceNames.isEmpty()
                                              ? null // Pass null to skip source filtering if no preferences
                                              : preferredSourceNames;

        String keywordToQuery = preferredKeywords.isEmpty()
                                  ? "%%" // Match all if no keywords specified
                                  : "%" + String.join("%", preferredKeywords) + "%"; // Match any article containing any preferred keyword

        Boolean isReadFilter = null;
        if (readStatusFilter == ReadStatusFilter.READ) {
            isReadFilter = true;
        } else if (readStatusFilter == ReadStatusFilter.UNREAD) {
            isReadFilter = false;
        }

        return articleRepository.findPersonalizedArticlesWithReadStatusFilter(
                categoriesToQuery,
                keywordToQuery,
                sourceNamesToQuery,
                userId,
                isReadFilter,
                pageable
        );
    }

    /**
     * Saves a new article or updates an existing one if the URL already exists.
     * This is primarily used by the news ingestion service.
     * @param article The article to save.
     * @return The saved or updated Article object.
     */
    public Article saveArticle(Article article) {
        return articleRepository.findByUrl(article.getUrl())
                .map(existingArticle -> {
                    // Only update if the new article is more recent
                    if (article.getPublishedDate().isAfter(existingArticle.getPublishedDate())) {
                        existingArticle.setTitle(article.getTitle());
                        existingArticle.setDescription(article.getDescription());
                        existingArticle.setImageUrl(article.getImageUrl());
                        existingArticle.setPublishedDate(article.getPublishedDate());
                        existingArticle.setAuthor(article.getAuthor());
                        existingArticle.setCategory(article.getCategory());
                        // Keep view count as is, or reset if desired. Usually, you retain view count.
                        return articleRepository.save(existingArticle);
                    }
                    return existingArticle; // Return the existing article if newer
                })
                .orElseGet(() -> articleRepository.save(article)); // Save new if not found
    }

    /**
     * Get an article by its ID.
     * Increments the viewCount of the article.
     * If a userId is provided, it also marks the article as read for that user.
     * @param id The ID of the article.
     * @param userId Optional: The ID of the authenticated user.
     * @return The Article object.
     * @throws ResourceNotFoundException if the article or user is not found.
     */
    public Article getArticleById(Long id, Long userId) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));

        // Increment view count
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article); // Save the updated view count

        // Mark as read for authenticated user if userId is provided
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

            // Create or update read status
            UserArticleReadStatusId readStatusId = new UserArticleReadStatusId(userId, id);
            Optional<UserArticleReadStatus> existingStatus = readStatusRepository.findById(readStatusId);

            UserArticleReadStatus status;
            if (existingStatus.isPresent()) {
                status = existingStatus.get();
                status.setRead(true); // Mark as read
            } else {
                status = new UserArticleReadStatus(user, article, true); // New read status
            }
            readStatusRepository.save(status);
        }

        return article;
    }

    /**
     * Toggles the read status for a specific article for the authenticated user.
     * @param userId The ID of the user.
     * @param articleId The ID of the article.
     * @param markAsRead True to mark as read, false to mark as unread.
     * @throws ResourceNotFoundException if the user or article is not found.
     */
    public void toggleReadStatus(Long userId, Long articleId, boolean markAsRead) throws ResourceNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        // Use the composite ID to find the read status
        UserArticleReadStatusId id = new UserArticleReadStatusId(userId, articleId);
        Optional<UserArticleReadStatus> existingStatus = readStatusRepository.findById(id);

        if (existingStatus.isPresent()) {
            UserArticleReadStatus status = existingStatus.get();
            status.setRead(markAsRead); // Set the desired read status
            readStatusRepository.save(status);
        } else {
            // If no existing status, create a new one with the desired markAsRead value
            UserArticleReadStatus newStatus = new UserArticleReadStatus(user, article, markAsRead);
            readStatusRepository.save(newStatus);
        }
    }
}