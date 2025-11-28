package library.repository;

import library.domain.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory user repository.
 */
public class InMemoryUserRepository implements UserRepository {
    private final List<User> users = new ArrayList<>();

    @Override
    public void save(User user) { users.add(user); }

    @Override
    public Optional<User> findById(String id) { return users.stream().filter(u -> u.getId().equals(id)).findFirst(); }

    @Override
    public Optional<User> findByEmail(String email) { return users.stream().filter(u -> u.getEmail().equals(email)).findFirst(); }

    @Override
    public List<User> findAll() { return new ArrayList<>(users); }

    @Override
    public void delete(User user) { users.remove(user); }
}
