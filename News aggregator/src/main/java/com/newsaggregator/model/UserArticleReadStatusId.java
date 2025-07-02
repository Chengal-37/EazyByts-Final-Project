package com.newsaggregator.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserArticleReadStatusId implements Serializable {

    private Long userId;
    private Long articleId;

    public UserArticleReadStatusId() {
    }

    public UserArticleReadStatusId(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public Long getArticleId() {
        return articleId;
    }

    // Setters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    // hashCode and equals are crucial for composite keys
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserArticleReadStatusId that = (UserArticleReadStatusId) o;
        return Objects.equals(userId, that.userId) &&
               Objects.equals(articleId, that.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, articleId);
    }
}