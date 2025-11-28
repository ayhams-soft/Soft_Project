package library.repository;

import library.domain.media.Book;
import library.domain.media.CD;
import library.domain.media.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of the MediaRepository.
 * Stores all media items (books and CDs) in a list.
 */
public class InMemoryMediaRepository implements MediaRepository {

    private final List<Media> items = new ArrayList<>();

    /**
     * Saves a media item to the repository.
     *
     * @param media the media object to store
     */
    @Override
    public void save(Media media) { 
        items.add(media); 
    }

    /**
     * Finds a media item by its id.
     *
     * @param id the media id
     * @return Optional containing the media if found, otherwise empty
     */
    @Override
    public Optional<Media> findById(String id) { 
        return items.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst(); 
    }

    /**
     * Searches media by title, author, artist, ISBN, or id.
     * The search is case-insensitive.
     *
     * @param q the search query
     * @return list of matching media items
     */
    @Override
    public List<Media> search(String q) {
        String ql = q == null ? "" : q.toLowerCase();

        return items.stream().filter(m -> {
            boolean titleMatch = m.getTitle().toLowerCase().contains(ql);
            boolean authorOrArtist = false;

            if (m instanceof Book) {
                Book b = (Book) m;
                authorOrArtist =
                        b.getAuthor().toLowerCase().contains(ql) ||
                        b.getIsbn().toLowerCase().contains(ql);
            } else if (m instanceof CD) {
                authorOrArtist =
                        ((CD) m).getArtist().toLowerCase().contains(ql);
            }

            return titleMatch || authorOrArtist || m.getId().toLowerCase().contains(ql);
        }).collect(Collectors.toList());
    }

    /**
     * Returns all stored media items.
     *
     * @return list of all media
     */
    @Override
    public List<Media> findAll() { 
        return new ArrayList<>(items); 
    }
}
