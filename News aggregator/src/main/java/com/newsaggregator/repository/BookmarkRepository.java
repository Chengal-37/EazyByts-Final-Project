package com.newsaggregator.repository;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Bookmark;
import com.newsaggregator.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // NEW: Import for List
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    // Find all bookmarks for a specific user, with pagination
    Page<Bookmark> findByUserOrderByBookmarkedDateDesc(User user, Pageable pageable);

    // Check if a specific article is bookmarked by a specific user
    Optional<Bookmark> findByUserAndArticle(User user, Article article);

    // Delete a bookmark by user and article
    void deleteByUserAndArticle(User user, Article article);

    // Count bookmarks for a specific article (could indicate popularity)
    long countByArticle(Article article);

    /**
     * Finds all bookmarks for a specific user within a given list of article IDs.
     * This method is crucial for efficiently populating the 'isBookmarked' flag for a page of articles
     * in the controller layer by performing a single bulk query.
     *
     * @param userId The ID of the user.
     * @param articleIds A list of article IDs to check for bookmarks.
     * @return A list of Bookmark entities matching the criteria.
     */
    List<Bookmark> findByUserIdAndArticleIdIn(Long userId, List<Long> articleIds); // NEW method
}