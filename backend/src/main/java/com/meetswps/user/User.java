package com.meetswps.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Only the first name is ever shown to other students. */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    /** Set by a moderator; a banned account cannot log in and is never shown to anyone. */
    @Column(nullable = false)
    private boolean banned;

    @Column(name = "privacy_policy_version", length = 20)
    private String privacyPolicyVersion;

    @Column(name = "privacy_policy_accepted_at")
    private Instant privacyPolicyAcceptedAt;

    protected User() {
    }

    public User(String email, String passwordHash, String firstName, Role role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void rename(String firstName) {
        this.firstName = firstName;
    }

    public Role getRole() {
        return role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void markEmailVerified() {
        this.emailVerified = true;
    }

    public boolean isBanned() {
        return banned;
    }

    public void ban() {
        this.banned = true;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void acceptPrivacyPolicy(String version) {
        this.privacyPolicyVersion = version;
        this.privacyPolicyAcceptedAt = Instant.now();
    }
}
