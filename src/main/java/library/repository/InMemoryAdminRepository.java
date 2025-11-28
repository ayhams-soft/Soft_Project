package library.repository;

import library.domain.Admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Simple in-memory implementation of the AdminRepository.
 * Stores admins inside a list (not persistent).
 */
public class InMemoryAdminRepository implements AdminRepository {

    private final List<Admin> admins = new ArrayList<>();

    /**
     * Saves the admin object to the internal list.
     *
     * @param admin the admin to save
     */
    @Override
    public void save(Admin admin) {
        admins.add(admin);
    }

    /**
     * Searches for an admin by username.
     *
     * @param username the username to look for
     * @return Optional containing the admin if found, otherwise empty
     */
    @Override
    public Optional<Admin> findByUsername(String username) {
        return admins.stream()
                .filter(a -> a.getUsername().equals(username))
                .findFirst();
    }
}
