package library.repository;

import library.domain.media.Media;

import java.util.List;
import java.util.Optional;

public interface MediaRepository {
    void save(Media media);
    Optional<Media> findById(String id);
    List<Media> search(String q);
    List<Media> findAll();
}
