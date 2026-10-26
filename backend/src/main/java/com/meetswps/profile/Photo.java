package com.meetswps.profile;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** A profile photo. The file is kept in {@link com.meetswps.storage.FileStorage} under the photo's id. */
@Entity
@Table(name = "photos")
public class Photo {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(nullable = false)
    private int position;

    @Column(name = "content_type", nullable = false, length = 50)
    private String contentType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Photo() {
    }

    Photo(UUID userId, int position, String contentType) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.position = position;
        this.contentType = contentType;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public int getPosition() {
        return position;
    }

    void moveTo(int position) {
        this.position = position;
    }

    public String getContentType() {
        return contentType;
    }

    public String storageKey() {
        return id.toString();
    }
}
