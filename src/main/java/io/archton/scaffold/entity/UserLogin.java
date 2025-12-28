package io.archton.scaffold.entity;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.PasswordType;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.Instant;

@Entity
@Table(name = "user_login")
@UserDefinition
public class UserLogin extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Username
    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    public String email;

    @Password(PasswordType.MCF)
    @NotBlank
    @Column(nullable = false)
    public String password;

    @Roles
    @Column(nullable = false)
    public String role = "user";

    @Size(max = 100)
    @Column(name = "first_name")
    public String firstName;

    @Size(max = 100)
    @Column(name = "last_name")
    public String lastName;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(nullable = false)
    public boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        normalizeEmail();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
        normalizeEmail();
    }

    private void normalizeEmail() {
        if (email != null) {
            email = email.toLowerCase().trim();
        }
    }

    // --- Factory Methods ---

    /**
     * Create a new user with hashed password.
     * Password is hashed using BCrypt with cost factor 12.
     */
    public static UserLogin create(String email, String plainPassword, String role) {
        UserLogin user = new UserLogin();
        user.email = email.toLowerCase().trim();
        user.password = BcryptUtil.bcryptHash(plainPassword, 12);
        user.role = role;
        return user;
    }

    // --- Finder Methods ---

    public static UserLogin findByEmail(String email) {
        return find("email", email.toLowerCase().trim()).firstResult();
    }

    public static boolean emailExists(String email) {
        return count("email", email.toLowerCase().trim()) > 0;
    }

    // --- Display Methods ---

    public String getDisplayName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return email;
    }
}
