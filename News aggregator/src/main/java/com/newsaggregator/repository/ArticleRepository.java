package com.newsaggregator.repository;

import com.newsaggregator.model.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;// Added for findPersonalizedArticlesWithReadStatusFilter to correctly type readArticleIds

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // --- Common Query Fragments for reusability and clarity ---
    // Changed to use UserArticleReadStatus, assuming it has fields user and article
    String JOIN_READ_STATUS = " LEFT JOIN UserArticleReadStatus uars ON a.id = uars.article.id AND uars.user.id = :userId ";
    String READ_STATUS_CONDITION = " (:isReadFilter IS NULL OR " +
                                   "   (:isReadFilter = TRUE AND uars.isRead = TRUE) OR " +
                                   "   (:isReadFilter = FALSE AND (uars.isRead = FALSE OR uars.isRead IS NULL)) ) "; // isRead IS NULL implies unread for new articles

    Optional<Article> findByUrl(String url);

    /**
     * Finds a paginated list of the latest news articles, optionally filtered by user-specific read status.
     * Orders by published date descending.
     *
     * @param userId The ID of the user. Can be null if not filtering by read status.
     * @param isReadFilter If TRUE, returns only read articles; if FALSE, returns only unread articles. If NULL, returns all.
     * @param pageable Pagination and sorting information.
     * @return A Page of Article entities.
     */
    @Query("SELECT a FROM Article a" +
            JOIN_READ_STATUS +
            "WHERE " + READ_STATUS_CONDITION +
            "ORDER BY a.publishedDate DESC")
    Page<Article> findLatestArticlesWithReadStatusFilter(
            @Param("userId") Long userId,
            @Param("isReadFilter") Boolean isReadFilter,
            Pageable pageable);

    /**
     * Finds a paginated list of top articles (assumed by published date for consistency, adjust to viewCount if needed for 'top headlines'),
     * optionally filtered by user-specific read status.
     *
     * @param userId The ID of the user. Can be null if not filtering by read status.
     * @param isReadFilter If TRUE, returns only read articles; if FALSE, returns only unread articles. If NULL, returns all.
     * @param pageable Pagination and sorting information.
     * @return A Page of Article entities.
     */
    @Query("SELECT a FROM Article a" +
            JOIN_READ_STATUS +
            "WHERE " + READ_STATUS_CONDITION +
            "ORDER BY a.publishedDate DESC") // This assumes 'top' is by latest published. If 'top' means 'most viewed', change to a.viewCount DESC.
    Page<Article> findTopArticlesWithReadStatusFilter( // Renamed for clarity, removed '100' as Pageable handles limit
            @Param("userId") Long userId,
            @Param("isReadFilter") Boolean isReadFilter,
            Pageable pageable);

    /**
     * Finds a paginated list of most viewed articles, optionally filtered by user-specific read status.
     * Orders by view count descending.
     *
     * @param userId The ID of the user. Can be null if not filtering by read status.
     * @param isReadFilter If TRUE, returns only read articles; if FALSE, returns only unread articles. If NULL, returns all.
     * @param pageable Pagination and sorting information.
     * @return A Page of Article entities.
     */
    @Query("SELECT a FROM Article a" +
            JOIN_READ_STATUS +
            "WHERE " + READ_STATUS_CONDITION +
            "ORDER BY a.viewCount DESC")
    Page<Article> findMostViewedArticlesWithReadStatusFilter(
            @Param("userId") Long userId,
            @Param("isReadFilter") Boolean isReadFilter,
            Pageable pageable);

    /**
     * Searches for articles based on a keyword and/or category, optionally filtered by user-specific read status.
     *
     * @param userId The ID of the user. Can be null if not filtering by read status.
     * @param keyword The keyword to search in title or description (can be null/empty).
     * @param category The category to filter by (can be null/empty).
     * @param isReadFilter If TRUE, returns only read articles; if FALSE, returns only unread articles. If NULL, returns all.
     * @param pageable Pagination and sorting information.
     * @return A Page of Article entities.
     */
    @Query("SELECT a FROM Article a" +
            JOIN_READ_STATUS +
            "WHERE (" + READ_STATUS_CONDITION + ")" +
            "AND (:keyword IS NULL OR :keyword = '' OR LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
            "AND (:category IS NULL OR :category = '' OR LOWER(a.category) = LOWER(:category))" +
            "ORDER BY a.publishedDate DESC")
    Page<Article> searchArticlesWithReadStatusFilter(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("isReadFilter") Boolean isReadFilter,
            Pageable pageable);

    /**
     * Finds personalized news articles for a user based on their preferences, optionally filtered by read status.
     * This query filters articles by preferred categories, keywords in title/description,
     * or preferred source names.
     *
     * IMPORTANT: For 'categories' and 'sourceNames' parameters, if the intent is to skip filtering
     * by them (e.g., user has no preferences), pass a `null` list. The query will then ignore that filter.
     * If you pass an *empty* list, the `IN` clause will generally result in no matches (because `element IN ()` is always false).
     *
     * @param categories List of preferred categories (lowercase). If null, this condition is skipped.
     * @param keyword Combined keyword for title/description search. If null or "%%", this condition is skipped.
     * @param sourceNames List of preferred source names (lowercase). If null, this condition is skipped.
     * @param userId The ID of the user. Can be null if not filtering by read status.
     * @param isReadFilter If TRUE, returns only read articles; if FALSE, returns only unread articles. If NULL, returns all.
     * @param pageable Pagination information.
     * @return A Page of Article objects matching the preferences.
     */
    @Query("SELECT a FROM Article a " +
            "JOIN a.source s " + // Inner join, assuming articles always have a source
            JOIN_READ_STATUS +
            "WHERE (" + READ_STATUS_CONDITION + ")" +
            // Handle categories: if categories is null, skip filter. Otherwise, check IN.
            // THIS WAS THE PRIMARY CAUSE OF THE ERROR. It now correctly checks if the List object is null.
            "AND (:categories IS NULL OR LOWER(a.category) IN (:categories))" +
            // Handle keyword: if null or "%%", skip filter. Otherwise, check like.
            "AND (:keyword IS NULL OR :keyword = '%%' OR LOWER(a.title) LIKE :keyword OR LOWER(a.description) LIKE :keyword)" +
            // Handle source names: if sourceNames is null, skip filter. Otherwise, check IN.
            // THIS WAS ALSO A CAUSE OF THE ERROR for sourceNames.
            "AND (:sourceNames IS NULL OR LOWER(s.name) IN (:sourceNames))" +
            "ORDER BY a.publishedDate DESC")
    Page<Article> findPersonalizedArticlesWithReadStatusFilter(
            @Param("categories") List<String> categories,
            @Param("keyword") String keyword,
            @Param("sourceNames") List<String> sourceNames,
            @Param("userId") Long userId,
            @Param("isReadFilter") Boolean isReadFilter,
            Pageable pageable);
}