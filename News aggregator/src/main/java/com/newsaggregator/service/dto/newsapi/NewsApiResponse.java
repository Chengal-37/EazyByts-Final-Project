package com.newsaggregator.service.dto.newsapi;

import java.util.List;

/**
 * DTO for the main response structure from NewsAPI.org.
 */
public class NewsApiResponse {
    private String status;
    private Integer totalResults;
    private List<Article> articles;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }

    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }
}

