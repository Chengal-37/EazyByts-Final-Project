package com.newsaggregator.repository;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserArticleReadStatus;
import com.newsaggregator.model.UserArticleReadStatusId; // Make sure this is imported
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying; // For @Modifying annotation
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional; // For @Transactional annotation

import java.util.List;
import java.util.Optional;

@Repository
public interface UserArticleReadStatusRepository extends JpaRepository<UserArticleReadStatus, UserArticleReadStatusId> {

    /**
     * Finds a specific read status record for a given user and article.
     *
     * @param user The User entity.
     * @param article The Article entity.
     * @return An Optional containing the UserArticleReadStatus if found, empty otherwise.
     */
    Optional<UserArticleReadStatus> findByUserAndArticle(User user, Article article);

    /**
     * Finds all read statuses for a specific user.
     * This could be useful for a user's "read history" or similar.
     *
     * @param user The User entity.
     * @return A list of UserArticleReadStatus objects for the given user.
     */
    List<UserArticleReadStatus> findByUser(User user);

    /**
     * **NEWLY ADDED:**
     * Finds read statuses for a specific user and a list of article IDs.
     * This is crucial for efficient bulk retrieval of read statuses in the ArticleController.
     *
     * @param userId The ID of the user.
     * @param articleIds A list of article IDs to check.
     * @return A list of UserArticleReadStatus objects for the given user and articles.
     */
    List<UserArticleReadStatus> findByUserIdAndArticleIdIn(Long userId, List<Long> articleIds);


    /**
     * Finds all articles marked as read by a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of Articles marked as read by the user.
     */
    @Query("SELECT uars.article FROM UserArticleReadStatus uars WHERE uars.user.id = :userId AND uars.isRead = true")
    List<Article> findReadArticlesByUserId(@Param("userId") Long userId);

    /**
     * Finds all articles marked as unread by a specific user.
     *
     * @param userId The ID of the user.
     * @return A list of Articles marked as unread by the user.
     */
    @Query("SELECT uars.article FROM UserArticleReadStatus uars WHERE uars.user.id = :userId AND uars.isRead = false")
    List<Article> findUnreadArticlesByUserId(@Param("userId") Long userId);

    /**
     * Checks if a specific article has been marked as read by a specific user.
     *
     * @param userId The ID of the user.
     * @param articleId The ID of the article.
     * @return True if the article is marked as read by the user, false otherwise.
     */
    @Query("SELECT COUNT(uars) > 0 FROM UserArticleReadStatus uars WHERE uars.user.id = :userId AND uars.article.id = :articleId AND uars.isRead = true")
    boolean existsByUserIdAndArticleIdAndIsReadTrue(@Param("userId") Long userId, @Param("articleId") Long articleId);

    /**
     * Deletes all read status records for a given article.
     * @param articleId The ID of the article.
     * @return The number of records deleted.
     */
    @Modifying // Indicates that this query will modify the database
    @Transactional // Ensures the operation runs within a transaction
    int deleteById_ArticleId(Long articleId); // Corrected for @EmbeddedId

    /**
     * Deletes all read status records for a given user.
     * @param userId The ID of the user.
     * @return The number of records deleted.
     */
    @Modifying // Indicates that this query will modify the database
    @Transactional // Ensures the operation runs within a transaction
    int deleteById_UserId(Long userId); // Corrected for @EmbeddedId

    /**
     * Custom query to fetch a list of Article objects along with their read status for a given user.
     * Note: This query returns `Object[]` where `Object[0]` is an Article and `Object[1]` is its boolean read status.
     *
     * @param articleIds The list of article IDs to check.
     * @param userId The ID of the user.
     * @return A list of Object arrays, where each array contains an Article and its read status (boolean).
     */
    @Query("SELECT a, uars.isRead " + // FIX: Removed redundant CASE WHEN, simplified to uars.isRead
           "FROM Article a LEFT JOIN UserArticleReadStatus uars ON a.id = uars.article.id AND uars.user.id = :userId " +
           "WHERE a.id IN :articleIds")
    List<Object[]> findArticlesWithReadStatusForUser(@Param("articleIds") List<Long> articleIds, @Param("userId") Long userId);

}