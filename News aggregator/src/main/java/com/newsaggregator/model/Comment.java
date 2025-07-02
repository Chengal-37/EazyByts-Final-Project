package com.newsaggregator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_comment_article_id", columnList = "article_id"),
    @Index(name = "idx_comment_user_id", columnList = "user_id"),
    @Index(name = "idx_comment_created_at", columnList = "createdAt")
})
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who posted the comment

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article; // The article being commented on

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // The actual comment text

    @Column(nullable = false)
    private LocalDateTime createdAt; // Timestamp when the comment was created

    // Optional: for nested comments/replies
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // If this comment is a reply to another comment

    // Constructors
    public Comment() {
        this.createdAt = LocalDateTime.now(); // Set creation time automatically
    }

    public Comment(User user, Article article, String content) {
        this.user = user;
        this.article = article;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Comment(User user, Article article, String content, Comment parentComment) {
        this.user = user;
        this.article = article;
        this.content = content;
        this.parentComment = parentComment;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    @Override
    public String toString() {
        return "Comment{" +
               "id=" + id +
               ", userId=" + (user != null ? user.getId() : "null") +
               ", articleId=" + (article != null ? article.getId() : "null") +
               ", content='" + content.substring(0, Math.min(content.length(), 50)) + "...'" +
               ", createdAt=" + createdAt +
               '}';
    }
}
