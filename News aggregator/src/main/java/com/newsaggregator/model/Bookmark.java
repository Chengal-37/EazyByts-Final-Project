package com.newsaggregator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference; // Import this

@Entity
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "article_id"}) // Ensure a user can only bookmark an article once
})
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference("user-bookmarks") // <--- ADDED ANNOTATION
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    @JsonBackReference("article-bookmarks") // <--- ADDED ANNOTATION
    private Article article;

    @Column(nullable = false)
    private LocalDateTime bookmarkedDate;

    // Constructors
    public Bookmark() {
        this.bookmarkedDate = LocalDateTime.now(); // Set creation time automatically
    }

    public Bookmark(User user, Article article) {
        this.user = user;
        this.article = article;
        this.bookmarkedDate = LocalDateTime.now();
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

    public LocalDateTime getBookmarkedDate() {
        return bookmarkedDate;
    }

    public void setBookmarkedDate(LocalDateTime bookmarkedDate) {
        this.bookmarkedDate = bookmarkedDate;
    }

    @Override
    public String toString() {
        return "Bookmark{" +
               "id=" + id +
               ", userId=" + (user != null ? user.getId() : "null") +
               ", articleId=" + (article != null ? article.getId() : "null") +
               ", bookmarkedDate=" + bookmarkedDate +
               '}';
    }
}
