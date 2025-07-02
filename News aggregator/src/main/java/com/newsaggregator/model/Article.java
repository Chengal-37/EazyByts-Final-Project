package com.newsaggregator.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "articles", indexes = {
    @Index(name = "idx_article_publisheddate", columnList = "publishedDate"),
    @Index(name = "idx_article_category", columnList = "category"),
    @Index(name = "idx_article_sourceid", columnList = "source_id") // Index on foreign key
})
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 512)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true, length = 1024)
    private String url;

    private String imageUrl;

    @Column(nullable = false)
    private LocalDateTime publishedDate;

    private String category;

    private String author;

    @Column(nullable = false)
    private long viewCount = 0;


    // Many-to-one relationship with Source
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id", nullable = false)
    @JsonBackReference
    private Source source;

    // Bookmarks associated with this article
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("article-bookmarks")
    private Set<Bookmark> bookmarks = new HashSet<>();

    // NEW: Read statuses associated with this article by different users
    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("article-read-status") // Ensure a unique reference name
    private Set<UserArticleReadStatus> readStatuses = new HashSet<>();


    // Constructors
    public Article() {
    }

    public Article(String title, String description, String url, String imageUrl, LocalDateTime publishedDate, String category, String author, Source source) {
        this.title = title;
        this.description = description;
        this.url = url;
        this.imageUrl = imageUrl;
        this.publishedDate = publishedDate;
        this.category = category;
        this.author = author;
        this.source = source;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public Set<Bookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(Set<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    public long getViewCount() {
        return viewCount;
    }

    public void setViewCount(long viewCount) {
        this.viewCount = viewCount;
    }

    // NEW: Getter and Setter for readStatuses
    public Set<UserArticleReadStatus> getReadStatuses() {
        return readStatuses;
    }

    public void setReadStatuses(Set<UserArticleReadStatus> readStatuses) {
        this.readStatuses = readStatuses;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", publishedDate=" + publishedDate +
                ", category='" + category + '\'' +
                ", source=" + (source != null ? source.getName() : "null") +
                ", viewCount=" + viewCount +
                '}';
    }
}