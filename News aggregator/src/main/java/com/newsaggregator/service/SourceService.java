package com.newsaggregator.service;

import com.newsaggregator.exception.ResourceNotFoundException;
import com.newsaggregator.model.Source;
import com.newsaggregator.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SourceService {

    @Autowired
    private SourceRepository sourceRepository;

    /**
     * Finds all news sources.
     * @return A list of Source objects.
     */
    public List<Source> getAllSources() {
        return sourceRepository.findAll();
    }

    /**
     * Finds a source by its ID.
     * @param id The ID of the source.
     * @return An Optional containing the Source if found, empty otherwise.
     */
    public Optional<Source> getSourceById(Long id) {
        return sourceRepository.findById(id);
    }

    /**
     * Finds a source by its name.
     * @param name The name of the source.
     * @return An Optional containing the Source if found, empty otherwise.
     */
    public Optional<Source> getSourceByName(String name) {
        return sourceRepository.findByName(name);
    }

    /**
     * Saves a new source. If a source with the same name already exists, it might
     * return the existing one or throw an exception depending on desired behavior.
     * For now, it will check if it exists by name and return existing, or save new.
     * @param source The source to save.
     * @return The saved or existing Source object.
     */
    public Source saveSource(Source source) {
        // Check if source with this name already exists
        return sourceRepository.findByName(source.getName())
                .orElseGet(() -> sourceRepository.save(source));
    }

    /**
     * Updates an existing source.
     * @param id The ID of the source to update.
     * @param updatedSource The Source object with updated data.
     * @return The updated Source object.
     * @throws ResourceNotFoundException if the source with the given ID is not found.
     */
    public Source updateSource(Long id, Source updatedSource) {
        Source existingSource = sourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + id));

        existingSource.setName(updatedSource.getName());
        existingSource.setBaseUrl(updatedSource.getBaseUrl());
        existingSource.setRssFeedUrl(updatedSource.getRssFeedUrl());
        existingSource.setApiKey(updatedSource.getApiKey());

        return sourceRepository.save(existingSource);
    }

    /**
     * Deletes a source by its ID.
     * @param id The ID of the source to delete.
     * @throws ResourceNotFoundException if the source with the given ID is not found.
     */
    public void deleteSource(Long id) {
        if (!sourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Source not found with id: " + id);
        }
        sourceRepository.deleteById(id);
    }
}

