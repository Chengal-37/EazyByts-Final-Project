package com.newsaggregator.service.dto.newsapi;

/**
 * DTO for an individual article within the NewsAPI.org response.
 */
public class Article {
    private Source source; // Nested Source DTO for NewsAPI
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt; // ISO 8601 string
    private String content;

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Article{" +
               "title='" + title + '\'' +
               ", url='" + url + '\'' +
               ", publishedAt='" + publishedAt + '\'' +
               '}';
    }
}

