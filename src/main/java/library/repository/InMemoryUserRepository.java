package library.repository;

import library.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simple in-memory implementation of the UserRepository.
 * Stores users inside a list (not persistent).
 */
public class InMemoryUserRepository implements UserRepository {

    private final List<User> users = new ArrayList<>();

    /**
     * Saves a new user to the repository.
     *
     * @param user the user object to store
     */
    @Override
    public void save(User user) {
        users.add(user);
    }

    /**
     * Finds a user by id.
     *
     * @param id the user id
     * @return Optional containing the user if found, otherwise empty
     */
    @Override
    public Optional<User> findById(String id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    /**
     * Finds a user by email.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found, otherwise empty
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return users.stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    /**
     * Returns all users stored in the repository.
     *
     * @return list of all users
     */
    @Override
    public List<User> findAll() {
        return new ArrayList<>(users);
    }

    /**
     * Removes a user from the repository.
     *
     * @param user the user to delete
     */
    @Override
    public void delete(User user) {
        users.remove(user);
    }
}
