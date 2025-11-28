package library.repository;

import library.domain.Admin;

import java.util.Optional;

public interface AdminRepository {
    void save(Admin admin);
    Optional<Admin> findByUsername(String username);
}
