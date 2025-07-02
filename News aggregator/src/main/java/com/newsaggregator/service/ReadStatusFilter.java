package com.newsaggregator.service; // Or com.newsaggregator.util or com.newsaggregator.enums

public enum ReadStatusFilter {
    ALL,      // Return all articles, regardless of read status
    READ,     // Return only articles marked as read by the user
    UNREAD    // Return only articles not marked as read by the user
}