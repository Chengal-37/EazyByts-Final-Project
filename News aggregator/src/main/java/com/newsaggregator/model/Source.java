package com.newsaggregator.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonManagedReference; // Import this

@Entity
@Table(name = "sources")
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // e.g., "BBC News", "TechCrunch"

    @Column(nullable = false)
    private String baseUrl; // e.g., "https://www.bbc.com/news"

    private String rssFeedUrl; // URL for RSS feed, can be null if using API

    private String apiKey; // API key if source requires one (store securely or reference from config)

    @OneToMany(mappedBy = "source", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // <--- ADD THIS ANNOTATION
    private Set<Article> articles = new HashSet<>();

    // Constructors
    public Source() {
    }

    public Source(String name, String baseUrl, String rssFeedUrl, String apiKey) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.rssFeedUrl = rssFeedUrl;
        this.apiKey = apiKey;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRssFeedUrl() {
        return rssFeedUrl;
    }

    public void setRssFeedUrl(String rssFeedUrl) {
        this.rssFeedUrl = rssFeedUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Set<Article> getArticles() {
        return articles;
    }

    public void setArticles(Set<Article> articles) {
        this.articles = articles;
    }

    @Override
    public String toString() {
        return "Source{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", baseUrl='" + baseUrl + '\'' +
                '}';
    }
}
