package com.newsaggregator.dto;

import java.time.LocalDateTime;

public class ArticleResponseDTO {
    private Long id;
    private SourceResponseDTO source; // Use the new SourceResponseDTO
    private String author;
    private String title;
    private String description;
    private String url;
    private String imageUrl; // Maps to urlToImage from NewsAPI, but consistent with your model
    private LocalDateTime publishedDate; // Maps to publishedAt from NewsAPI, but as LocalDateTime
    private String category; // From your Article model
    private Long viewCount; // From your Article model

    // User-specific flags
    private Boolean isRead;
    private Boolean isBookmarked;

    // Constructors (optional, but good practice for easy mapping)
    public ArticleResponseDTO() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SourceResponseDTO getSource() {
        return source;
    }

    public void setSource(SourceResponseDTO source) {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public void setViewCount(Long viewCount) {
        this.viewCount = viewCount;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean isRead) {
        this.isRead = isRead;
    }

    public Boolean getIsBookmarked() {
        return isBookmarked;
    }

    public void setIsBookmarked(Boolean isBookmarked) {
        this.isBookmarked = isBookmarked;
    }
}
