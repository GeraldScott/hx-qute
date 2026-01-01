package io.archton.scaffold.repository;

import io.archton.scaffold.entity.UserLogin;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserLoginRepository implements PanacheRepository<UserLogin> {

    /**
     * Find user by email (case-insensitive).
     */
    public Optional<UserLogin> findByEmail(String email) {
        return find("email", email.toLowerCase().trim()).firstResultOptional();
    }

    /**
     * Check if email exists (case-insensitive).
     */
    public boolean emailExists(String email) {
        return count("email", email.toLowerCase().trim()) > 0;
    }

    /**
     * Check if email exists for a different user (for update validation).
     */
    public boolean existsByEmailAndIdNot(String email, Long id) {
        return count("email = ?1 AND id != ?2", email.toLowerCase().trim(), id) > 0;
    }
}
