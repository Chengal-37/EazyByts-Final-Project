package com.newsaggregator.dto;

public class SourceResponseDTO {
    private Long id; // Your internal database ID for the source
    private String name;
    private String apiId; // External API ID if applicable (e.g., NewsAPI's source ID)

    // Constructor for mapping from your model.Source
    public SourceResponseDTO(Long id, String name, String apiId) {
        this.id = id;
        this.name = name;
        this.apiId = apiId;
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

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }
}
