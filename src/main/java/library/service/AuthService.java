
package library.service;

import library.domain.Admin;
import library.exception.NotAuthorizedException;
import library.repository.AdminRepository;

import java.util.Optional;

/**
 * Simple authentication service for handling admin login and logout.
 */
public class AuthService {

    private final AdminRepository adminRepository;
    private Admin currentAdmin;

    /**
     * Creates a new AuthService using the given repository.
     *
     * @param adminRepository repository used to look up admin accounts
     */
    public AuthService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    /**
     * Attempts to log in an admin.
     *
     * @param username the username
     * @param password the password
     * @return true if login was successful, false otherwise
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
     * Logs out the currently logged-in admin.
     */
    public void logout() {
        currentAdmin = null;
    }

    /**
     * Ensures an admin is logged in.
     * Throws NotAuthorizedException if no admin session exists.
     */
    public void requireAdmin() {
        if (currentAdmin == null) {
            throw new NotAuthorizedException("Admin required");
        }
    }

    /**
     * @return true if an admin is currently logged in
     */
    public boolean isLoggedIn() {
        return currentAdmin != null;
    }
}
