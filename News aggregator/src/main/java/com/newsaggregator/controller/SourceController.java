package com.newsaggregator.controller;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Source;
import com.newsaggregator.security.payload.response.MessageResponse;
import com.newsaggregator.service.SourceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600) // Adjust CORS for production
@RestController
@RequestMapping("/sources")
public class SourceController {

    @Autowired
    private SourceService sourceService;

    /**
     * Retrieves all news sources.
     * Accessible by anyone.
     *
     * @return ResponseEntity with a list of Source objects.
     */
    @GetMapping
    public ResponseEntity<List<Source>> getAllSources() {
        List<Source> sources = sourceService.getAllSources();
        return ResponseEntity.ok(sources);
    }

    /**
     * Retrieves a single source by its ID.
     * Accessible by anyone.
     *
     * @param id The ID of the source.
     * @return ResponseEntity with the Source object.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Source> getSourceById(@PathVariable Long id) {
        return sourceService.getSourceById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    /**
     * Creates a new news source.
     * Requires ADMIN role.
     *
     * @param source The Source object to create.
     * @return ResponseEntity with the created Source object.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSource(@Valid @RequestBody Source source) {
        try {
            Source createdSource = sourceService.saveSource(source);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSource);
        } catch (Exception e) {
            // Potentially handle cases where source name already exists more gracefully
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error creating source: " + e.getMessage()));
        }
    }

    /**
     * Updates an existing news source.
     * Requires ADMIN role.
     *
     * @param id The ID of the source to update.
     * @param source The updated Source object.
     * @return ResponseEntity with the updated Source object.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSource(@PathVariable Long id, @Valid @RequestBody Source source) {
        try {
            Source updatedSource = sourceService.updateSource(id, source);
            return ResponseEntity.ok(updatedSource);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error updating source: " + e.getMessage()));
        }
    }

    /**
     * Deletes a news source.
     * Requires ADMIN role.
     *
     * @param id The ID of the source to delete.
     * @return ResponseEntity with a success message.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSource(@PathVariable Long id) {
        try {
            sourceService.deleteSource(id);
            return ResponseEntity.ok(new MessageResponse("Source deleted successfully!"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("Error deleting source: " + e.getMessage()));
        }
    }
}

