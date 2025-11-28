package library.repository;

import library.domain.Admin;
import java.util.Optional;

/**
 * Repository interface for storing and retrieving admins.
 */
public interface AdminRepository {

    /**
     * Saves a new admin to the repository.
     *
     * @param admin the admin object to save
     */
    void save(Admin admin);

    /**
     * Finds an admin by username.
     *
     * @param username the username to search for
     * @return Optional containing the admin if found, otherwise empty
     */
    Optional<Admin> findByUsername(String username);
}
