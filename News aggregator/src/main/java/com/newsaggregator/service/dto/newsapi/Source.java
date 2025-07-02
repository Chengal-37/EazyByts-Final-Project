package com.newsaggregator.service.dto.newsapi;

/**
 * DTO for the nested source object within NewsAPI.org article response.
 */
public class Source {
    private String id; // Source ID from NewsAPI, can be null
    private String name; // Source name from NewsAPI

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

