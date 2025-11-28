package library.service;

import library.domain.Admin;
import library.exception.NotAuthorizedException;
import library.repository.AdminRepository;

import java.util.Optional;

/**
 * Auth service handles simple admin login/logout.
 */
public class AuthService {
    private final AdminRepository adminRepository;
    private Admin currentAdmin;

    public AuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * Login admin. Returns true if success.
     */
    public boolean login(String username, String password) {
        Optional<Admin> a = adminRepository.findByUsername(username);
        if (!a.isPresent()) return false;
        Admin admin = a.get();
        if (admin.getPasswordHash().equals(password)) {
            currentAdmin = admin;
            return true;
        }
        return false;
    }

    /**
     * Logout current admin.
     */
    public void logout() {
        currentAdmin = null;
    }

    /**
     * Require admin session or throw.
     */
    public void requireAdmin() {
        if (currentAdmin == null) throw new NotAuthorizedException("Admin required");
    }

    public boolean isLoggedIn() {
        return currentAdmin != null;
    }
}
