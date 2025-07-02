package com.newsaggregator.controller;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Comment;
import com.newsaggregator.security.payload.response.MessageResponse;
import com.newsaggregator.security.services.UserDetailsImpl;
import com.newsaggregator.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust CORS for production
@RestController
@RequestMapping("/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * Adds a new comment to an article.
     * Requires user authentication (ROLE_USER or higher).
     *
     * @param authentication The Spring Security Authentication object.
     * @param requestBody A map containing articleId, content, and optionally parentCommentId.
     * @return ResponseEntity with the created Comment object or an error message.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addComment(
            Authentication authentication,
            @RequestBody Map<String, Object> requestBody) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            Long articleId = Long.valueOf(requestBody.get("articleId").toString());
            String content = requestBody.get("content").toString();
            Long parentCommentId = requestBody.containsKey("parentCommentId") ? Long.valueOf(requestBody.get("parentCommentId").toString()) : null;

            Comment comment = commentService.addComment(userId, articleId, content, parentCommentId);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error adding comment: " + e.getMessage()));
        }
    }

    /**
     * Retrieves all comments for a specific article.
     * Accessible by anyone (unauthenticated users).
     *
     * @param articleId The ID of the article.
     * @return ResponseEntity with a list of Comment objects.
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<?> getCommentsByArticle(@PathVariable Long articleId) {
        try {
            List<Comment> comments = commentService.getCommentsByArticle(articleId);
            return ResponseEntity.ok(comments);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error retrieving comments: " + e.getMessage()));
        }
    }

    /**
     * Deletes a comment by its ID.
     * Requires ADMIN or MODERATOR role, or the user who posted the comment.
     * Note: For user-specific deletion, you'd need to add logic in service to check ownership.
     *
     * @param commentId The ID of the comment to delete.
     * @return ResponseEntity with a success message or an error message.
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')") // You might add "or @commentService.isOwner(#commentId, authentication.principal.id)"
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.ok(new MessageResponse("Comment deleted successfully!"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting comment: " + e.getMessage()));
        }
    }

    /**
     * Updates the content of an existing comment.
     * Requires ADMIN or MODERATOR role, or the user who posted the comment.
     *
     * @param commentId The ID of the comment to update.
     * @param requestBody A map containing the new 'content'.
     * @return ResponseEntity with the updated Comment object or an error message.
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')") // Similar ownership check could be added
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, String> requestBody) {
        try {
            String newContent = requestBody.get("content");
            if (newContent == null || newContent.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse("Content cannot be empty."));
            }
            Comment updatedComment = commentService.updateComment(commentId, newContent);
            return ResponseEntity.ok(updatedComment);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating comment: " + e.getMessage()));
        }
    }
}
