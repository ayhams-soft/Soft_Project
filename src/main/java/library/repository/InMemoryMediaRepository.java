package library.repository;

import library.domain.media.Book;
import library.domain.media.CD;
import library.domain.media.Media;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory media repository.
 */
public class InMemoryMediaRepository implements MediaRepository {
    private final List<Media> items = new ArrayList<>();

    @Override
    public void save(Media media) { items.add(media); }

    @Override
    public Optional<Media> findById(String id) { return items.stream().filter(m -> m.getId().equals(id)).findFirst(); }

    @Override
    public List<Media> search(String q) {
        String ql = q == null ? "" : q.toLowerCase();
        return items.stream().filter(m -> {
            boolean titleMatch = m.getTitle().toLowerCase().contains(ql);
            boolean authorOrArtist = false;
            if (m instanceof Book) {
                authorOrArtist = ((Book)m).getAuthor().toLowerCase().contains(ql) || ((Book)m).getIsbn().toLowerCase().contains(ql);
            } else if (m instanceof CD) {
                authorOrArtist = ((CD)m).getArtist().toLowerCase().contains(ql);
            }
            return titleMatch || authorOrArtist || m.getId().toLowerCase().contains(ql);
        }).collect(Collectors.toList());
    }

    @Override
    public List<Media> findAll() { return new ArrayList<>(items); }
}
