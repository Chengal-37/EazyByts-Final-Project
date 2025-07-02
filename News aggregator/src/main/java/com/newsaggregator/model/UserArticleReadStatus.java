package com.newsaggregator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_article_read_status", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "article_id"})
})
public class UserArticleReadStatus {

    @EmbeddedId
    private UserArticleReadStatusId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId") // Maps the 'userId' field of the composite key
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId") // Maps the 'articleId' field of the composite key
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private boolean isRead = false; // Default to unread

    @Column(nullable = false)
    private LocalDateTime markedAt = LocalDateTime.now(); // Timestamp for when it was marked

    public UserArticleReadStatus() {
        this.id = new UserArticleReadStatusId(); // Initialize composite ID
    }

    public UserArticleReadStatus(User user, Article article, boolean isRead) {
        this.user = user;
        this.article = article;
        this.isRead = isRead;
        this.markedAt = LocalDateTime.now();
        this.id = new UserArticleReadStatusId(user.getId(), article.getId());
    }

    // Getters and Setters
    public UserArticleReadStatusId getId() {
        return id;
    }

    public void setId(UserArticleReadStatusId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        if (this.id == null) this.id = new UserArticleReadStatusId();
        this.id.setUserId(user.getId());
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
        if (this.id == null) this.id = new UserArticleReadStatusId();
        this.id.setArticleId(article.getId());
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
        this.markedAt = LocalDateTime.now(); // Update timestamp when status changes
    }

    public LocalDateTime getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(LocalDateTime markedAt) {
        this.markedAt = markedAt;
    }

    @PrePersist
    @PreUpdate
    public void setTimestamps() {
        if (markedAt == null) {
            markedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return "UserArticleReadStatus{" +
               "userId=" + (user != null ? user.getId() : "null") +
               ", articleId=" + (article != null ? article.getId() : "null") +
               ", isRead=" + isRead +
               ", markedAt=" + markedAt +
               '}';
    }
}