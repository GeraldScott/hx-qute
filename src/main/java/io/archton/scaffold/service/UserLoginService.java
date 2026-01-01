package io.archton.scaffold.service;

import io.archton.scaffold.entity.UserLogin;
import io.archton.scaffold.repository.UserLoginRepository;
import io.archton.scaffold.service.exception.UniqueConstraintException;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserLoginService {

    @Inject
    UserLoginRepository userLoginRepository;

    /**
     * Create a new user with hashed password.
     * Password is hashed using BCrypt with cost factor 12.
     *
     * @throws UniqueConstraintException if email already exists
     */
    @Transactional
    public UserLogin create(String email, String plainPassword, String role) {
        String normalizedEmail = email.toLowerCase().trim();

        // Validate unique constraint
        if (userLoginRepository.emailExists(normalizedEmail)) {
            throw new UniqueConstraintException("email", normalizedEmail,
                "A user with email '" + normalizedEmail + "' already exists.");
        }

        UserLogin user = new UserLogin();
        user.email = normalizedEmail;
        user.password = BcryptUtil.bcryptHash(plainPassword, 12);
        user.role = role;

        userLoginRepository.persist(user);
        return user;
    }

    /**
     * Check if email exists.
     */
    public boolean emailExists(String email) {
        return userLoginRepository.emailExists(email);
    }
}
