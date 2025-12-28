package io.archton.scaffold.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class PasswordValidator {

    @ConfigProperty(name = "app.security.password.min-length", defaultValue = "15")
    int minLength;

    @ConfigProperty(name = "app.security.password.max-length", defaultValue = "128")
    int maxLength;

    /**
     * Validate password against NIST SP 800-63B-4 requirements.
     *
     * @param password the plain text password to validate
     * @return list of validation errors (empty if valid)
     */
    public List<String> validate(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isEmpty()) {
            errors.add("Password is required");
            return errors;
        }

        // NIST 800-63B-4: Minimum 15 characters when password-only auth
        if (password.length() < minLength) {
            errors.add("Password must be at least " + minLength + " characters");
        }

        // NIST 800-63B-4: Accept at least 64 characters (we allow 128)
        if (password.length() > maxLength) {
            errors.add("Password must be " + maxLength + " characters or less");
        }

        // NIST 800-63B-4: No truncation - ensure full password is used
        // (This is enforced by not truncating in storage)

        // NIST 800-63B-4: No composition rules required
        // (Intentionally NOT checking for special chars, uppercase, etc.)

        return errors;
    }

    /**
     * Check if password is valid.
     */
    public boolean isValid(String password) {
        return validate(password).isEmpty();
    }
}
