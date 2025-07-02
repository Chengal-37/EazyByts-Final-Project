package com.newsaggregator.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "preference_type", "preference_value"}) // Ensure unique preferences per user
})
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String preferenceType; // e.g., "CATEGORY", "KEYWORD", "SOURCE"

    @Column(nullable = false)
    private String preferenceValue; // e.g., "Technology", "AI", "BBC News"

    // Constructors
    public UserPreference() {
    }

    public UserPreference(User user, String preferenceType, String preferenceValue) {
        this.user = user;
        this.preferenceType = preferenceType;
        this.preferenceValue = preferenceValue;
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

    public String getPreferenceType() {
        return preferenceType;
    }

    public void setPreferenceType(String preferenceType) {
        this.preferenceType = preferenceType;
    }

    public String getPreferenceValue() {
        return preferenceValue;
    }

    public void setPreferenceValue(String preferenceValue) {
        this.preferenceValue = preferenceValue;
    }

    @Override
    public String toString() {
        return "UserPreference{" +
               "id=" + id +
               ", userId=" + (user != null ? user.getId() : "null") +
               ", preferenceType='" + preferenceType + '\'' +
               ", preferenceValue='" + preferenceValue + '\'' +
               '}';
    }
}
