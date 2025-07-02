package com.newsaggregator.repository;

import com.newsaggregator.model.Article;
import com.newsaggregator.model.Comment;
import com.newsaggregator.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Find all comments for a specific article, ordered by creation date
    List<Comment> findByArticleOrderByCreatedAtAsc(Article article);

    // Find all top-level comments (no parent) for an article
    List<Comment> findByArticleAndParentCommentIsNullOrderByCreatedAtAsc(Article article);

    // Find replies to a specific parent comment
    List<Comment> findByParentCommentOrderByCreatedAtAsc(Comment parentComment);

    // Find comments made by a specific user, with pagination
    Page<Comment> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
}
