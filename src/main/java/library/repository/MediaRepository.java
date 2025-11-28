package library.repository;

import library.domain.media.Media;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing media items (books, CDs, etc.).
 */
public interface MediaRepository {

    /**
     * Saves a media item to the repository.
     *
     * @param media the media object to store
     */
    void save(Media media);

    /**
     * Finds a media item by its id.
     *
     * @param id the media id
     * @return Optional containing the media if found, otherwise empty
     */
    Optional<Media> findById(String id);

    /**
     * Searches for media items by title, author, artist, ISBN, or id.
     *
     * @param q the search query
     * @return list of matching media items
     */
    List<Media> search(String q);

    /**
     * Returns all stored media items.
     *
     * @return list of all media
     */
    List<Media> findAll();
}
