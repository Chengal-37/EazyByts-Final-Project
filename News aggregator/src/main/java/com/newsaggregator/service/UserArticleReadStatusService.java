package com.newsaggregator.service;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.User;
import com.newsaggregator.model.UserArticleReadStatus;
import com.newsaggregator.repository.ArticleRepository;
import com.newsaggregator.repository.UserArticleReadStatusRepository;
import com.newsaggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserArticleReadStatusService {

    private final UserArticleReadStatusRepository readStatusRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    @Autowired
    public UserArticleReadStatusService(UserArticleReadStatusRepository readStatusRepository,
                                        UserRepository userRepository,
                                        ArticleRepository articleRepository) {
        this.readStatusRepository = readStatusRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    /**
     * Marks an article as read for a given user. If the status already exists, it updates it.
     *
     * @param userId The ID of the user.
     * @param articleId The ID of the article.
     * @return The updated or newly created UserArticleReadStatus object.
     * @throws NoSuchElementException if the user or article is not found.
     */
    @Transactional
    public UserArticleReadStatus markArticleAsRead(Long userId, Long articleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NoSuchElementException("Article not found with ID: " + articleId));

        Optional<UserArticleReadStatus> existingStatus = readStatusRepository.findByUserAndArticle(user, article);

        UserArticleReadStatus status;
        if (existingStatus.isPresent()) {
            status = existingStatus.get();
            if (!status.isRead()) { // Only update if it was unread
                status.setRead(true);
                status.setMarkedAt(LocalDateTime.now());
            }
        } else {
            status = new UserArticleReadStatus(user, article, true);
        }
        return readStatusRepository.save(status);
    }

    /**
     * Marks an article as unread for a given user. If the status already exists and is read, it updates it.
     *
     * @param userId The ID of the user.
     * @param articleId The ID of the article.
     * @return The updated or newly created UserArticleReadStatus object.
     * @throws NoSuchElementException if the user or article is not found.
     */
    @Transactional
    public UserArticleReadStatus markArticleAsUnread(Long userId, Long articleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NoSuchElementException("Article not found with ID: " + articleId));

        Optional<UserArticleReadStatus> existingStatus = readStatusRepository.findByUserAndArticle(user, article);

        UserArticleReadStatus status;
        if (existingStatus.isPresent()) {
            status = existingStatus.get();
            if (status.isRead()) { // Only update if it was read
                status.setRead(false);
                status.setMarkedAt(LocalDateTime.now());
            }
        } else {
            // If it doesn't exist, and we're marking it unread, we can create a new record
            // However, typically, an unread status is implicit if no record exists.
            // This explicit creation ensures a record exists, perhaps for future tracking of "touched but unread".
            // Depending on requirements, you might choose NOT to create if it doesn't exist and isUnread.
            status = new UserArticleReadStatus(user, article, false);
        }
        return readStatusRepository.save(status);
    }

    /**
     * Toggles the read status of an article for a given user.
     * If no status exists, it marks it as read.
     *
     * @param userId The ID of the user.
     * @param articleId The ID of the article.
     * @return The updated or newly created UserArticleReadStatus object.
     * @throws NoSuchElementException if the user or article is not found.
     */
    @Transactional
    public UserArticleReadStatus toggleArticleReadStatus(Long userId, Long articleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found with ID: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new NoSuchElementException("Article not found with ID: " + articleId));

        Optional<UserArticleReadStatus> existingStatus = readStatusRepository.findByUserAndArticle(user, article);

        UserArticleReadStatus status;
        if (existingStatus.isPresent()) {
            status = existingStatus.get();
            status.setRead(!status.isRead()); // Toggle the status
            status.setMarkedAt(LocalDateTime.now()); // Update timestamp
        } else {
            // If no status exists, default to marking as read (common use case for "mark as read" button)
            status = new UserArticleReadStatus(user, article, true);
        }
        return readStatusRepository.save(status);
    }

    /**
     * Checks if a specific article is marked as read by a specific user.
     *
     * @param userId The ID of the user.
     * @param articleId The ID of the article.
     * @return true if the article is read by the user, false otherwise.
     */
    @Transactional(readOnly = true)
    public boolean isArticleReadByUser(Long userId, Long articleId) {
        // This leverages the custom query we added to the repository for efficiency
        return readStatusRepository.existsByUserIdAndArticleIdAndIsReadTrue(userId, articleId);
    }

    // You might add more methods here, e.g., to get a list of all read/unread articles for a user
    // or to delete read status records.
}
