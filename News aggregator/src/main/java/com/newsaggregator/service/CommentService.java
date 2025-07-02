package com.newsaggregator.service;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Article;
import com.newsaggregator.model.Comment;
import com.newsaggregator.model.User;
import com.newsaggregator.repository.ArticleRepository;
import com.newsaggregator.repository.CommentRepository;
import com.newsaggregator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ArticleRepository articleRepository;

    /**
     * Adds a new comment to an article.
     * @param userId The ID of the user posting the comment.
     * @param articleId The ID of the article being commented on.
     * @param content The content of the comment.
     * @param parentCommentId Optional ID of a parent comment if this is a reply.
     * @return The saved Comment object.
     * @throws ResourceNotFoundException if user or article is not found, or parent comment (if specified) is not found.
     */
    public Comment addComment(Long userId, Long articleId, String content, Long parentCommentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));

        Comment parentComment = null;
        if (parentCommentId != null) {
            parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found with id: " + parentCommentId));
            // Ensure parent comment belongs to the same article if you want strict hierarchy
            if (!parentComment.getArticle().getId().equals(articleId)) {
                throw new IllegalArgumentException("Parent comment does not belong to the specified article.");
            }
        }

        Comment comment = new Comment(user, article, content, parentComment);
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    /**
     * Retrieves all comments for a specific article, ordered by creation date.
     * @param articleId The ID of the article.
     * @return A list of Comment objects.
     * @throws ResourceNotFoundException if the article is not found.
     */
    public List<Comment> getCommentsByArticle(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + articleId));
        return commentRepository.findByArticleOrderByCreatedAtAsc(article);
    }

    /**
     * Deletes a comment by its ID.
     * @param commentId The ID of the comment to delete.
     * @throws ResourceNotFoundException if the comment is not found.
     */
    public void deleteComment(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new ResourceNotFoundException("Comment not found with id: " + commentId);
        }
        commentRepository.deleteById(commentId);
    }

    /**
     * Updates the content of an existing comment.
     * @param commentId The ID of the comment to update.
     * @param newContent The new content for the comment.
     * @return The updated Comment object.
     * @throws ResourceNotFoundException if the comment is not found.
     */
    public Comment updateComment(Long commentId, String newContent) {
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        existingComment.setContent(newContent);
        return commentRepository.save(existingComment);
    }
}

