package library.repository;

import library.domain.User;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing user records.
 */
public interface UserRepository {

    /**
     * Saves a user to the repository.
     *
     * @param user the user to store
     */
    void save(User user);

    /**
     * Finds a user by id.
     *
     * @param id the user id
     * @return Optional containing the user if found, otherwise empty
     */
    Optional<User> findById(String id);

    /**
     * Finds a user by email address.
     *
     * @param email the email to search for
     * @return Optional containing the user if found, otherwise empty
     */
    Optional<User> findByEmail(String email);

    /**
     * Returns all stored users.
     *
     * @return list of users
     */
    List<User> findAll();

    /**
     * Deletes a user from the repository.
     *
     * @param user the user to remove
     */
    void delete(User user);
}
