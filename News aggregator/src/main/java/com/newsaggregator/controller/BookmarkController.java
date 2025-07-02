package com.newsaggregator.controller;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Bookmark;
import com.newsaggregator.security.payload.response.MessageResponse;
import com.newsaggregator.security.services.UserDetailsImpl;
import com.newsaggregator.service.BookmarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust CORS for production
@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    @Autowired
    private BookmarkService bookmarkService;

    /**
     * Adds an article to the authenticated user's bookmarks.
     * Requires user authentication (ROLE_USER or higher).
     *
     * @param authentication The Spring Security Authentication object.
     * @param articleId The ID of the article to bookmark.
     * @return ResponseEntity with the created Bookmark object or an error message.
     */
    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> addBookmark(
            Authentication authentication,
            @RequestParam Long articleId) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            Bookmark bookmark = bookmarkService.addBookmark(userId, articleId);
            return ResponseEntity.status(HttpStatus.CREATED).body(bookmark);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error adding bookmark: " + e.getMessage()));
        }
    }

    /**
     * Retrieves a paginated list of bookmarks for the authenticated user.
     * Requires user authentication (ROLE_USER or higher).
     *
     * @param authentication The Spring Security Authentication object.
     * @param page The page number (0-indexed, default 0).
     * @param size The number of bookmarks per page (default 10).
     * @return ResponseEntity with a Page of Bookmark objects.
     */
    @GetMapping("/my-bookmarks")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Page<Bookmark>> getUserBookmarks(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();
        Page<Bookmark> bookmarks = bookmarkService.getUserBookmarks(userId, page, size);
        return ResponseEntity.ok(bookmarks);
    }

    /**
     * Deletes a bookmark for the authenticated user.
     * Requires user authentication (ROLE_USER or higher).
     *
     * @param authentication The Spring Security Authentication object.
     * @param articleId The ID of the article to unbookmark.
     * @return ResponseEntity with a success message or an error message.
     */
    @DeleteMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteBookmark(
            Authentication authentication,
            @RequestParam Long articleId) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            Long userId = userDetails.getId();
            bookmarkService.deleteBookmark(userId, articleId);
            return ResponseEntity.ok(new MessageResponse("Bookmark deleted successfully!"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting bookmark: " + e.getMessage()));
        }
    }
}
