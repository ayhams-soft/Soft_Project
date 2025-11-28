package library.repository;

import library.domain.Admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory admin repo.
 */
public class InMemoryAdminRepository implements AdminRepository {
    private final List<Admin> admins = new ArrayList<>();

    @Override
    public void save(Admin admin) {
        admins.add(admin);
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return admins.stream().filter(a -> a.getUsername().equals(username)).findFirst();
    }
}
