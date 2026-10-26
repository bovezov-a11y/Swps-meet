package com.meetswps.profile;

import com.meetswps.common.InvalidRequestException;
import com.meetswps.common.NotFoundException;
import com.meetswps.profile.ProfileDtos.PhotoResponse;
import com.meetswps.storage.FileStorage;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
class PhotoService {

    static final int MAX_PHOTOS = 3;

    private final PhotoRepository photos;
    private final ProfileRepository profiles;
    private final FileStorage storage;
    private final Audience audience;

    PhotoService(PhotoRepository photos, ProfileRepository profiles, FileStorage storage, Audience audience) {
        this.photos = photos;
        this.profiles = profiles;
        this.storage = storage;
        this.audience = audience;
    }

    @Transactional
    PhotoResponse upload(UUID userId, MultipartFile file) {
        if (!profiles.existsById(userId)) {
            throw new NotFoundException("Create your profile before adding photos");
        }
        List<Photo> existing = photos.findAllByUserIdOrderByPosition(userId);
        if (existing.size() >= MAX_PHOTOS) {
            throw new InvalidRequestException("file", "you can add up to " + MAX_PHOTOS + " photos");
        }
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("file", "choose a photo");
        }
        String contentType = detectImageType(file);
        Photo photo = new Photo(userId, existing.size(), contentType);
        photos.save(photo);
        try (InputStream content = file.getInputStream()) {
            storage.store(photo.storageKey(), content);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not store photo", e);
        }
        return PhotoResponse.from(photo);
    }

    @Transactional
    void delete(UUID userId, UUID photoId) {
        Photo photo = photos.findById(photoId).filter(p -> p.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Photo not found"));
        photos.delete(photo);
        storage.delete(photo.storageKey());
        renumber(photos.findAllByUserIdOrderByPosition(userId).stream().filter(p -> !p.getId().equals(photoId))
                .toList());
    }

    /** Puts the photos in the given order; the first one is the photo shown in lists. */
    @Transactional
    List<PhotoResponse> reorder(UUID userId, List<UUID> photoIds) {
        List<Photo> current = photos.findAllByUserIdOrderByPosition(userId);
        if (photoIds.size() != current.size() || !new HashSet<>(photoIds).equals(
                new HashSet<>(current.stream().map(Photo::getId).toList()))) {
            throw new InvalidRequestException("photoIds", "list each of your photos exactly once");
        }
        List<Photo> ordered = new ArrayList<>();
        for (UUID id : photoIds) {
            ordered.add(current.stream().filter(p -> p.getId().equals(id)).findFirst().orElseThrow());
        }
        renumber(ordered);
        return ordered.stream().map(PhotoResponse::from).toList();
    }

    /** A photo can be seen by its owner and by students who can currently see the owner. */
    @Transactional(readOnly = true)
    StoredPhoto load(UUID viewerId, UUID photoId) {
        Photo photo = photos.findById(photoId).orElseThrow(() -> new NotFoundException("Photo not found"));
        boolean visible = photo.getUserId().equals(viewerId) || audience.activeProfile(viewerId, photo.getUserId())
                .filter(owner -> canSee(viewerId, owner)).isPresent();
        if (!visible) {
            throw new NotFoundException("Photo not found");
        }
        return new StoredPhoto(storage.load(photo.storageKey()), photo.getContentType());
    }

    private boolean canSee(UUID viewerId, Profile owner) {
        return profiles.findById(viewerId).map(viewer -> !Audience.sharedIntents(viewer, owner).isEmpty()
                || audience.matched(viewerId, owner.getUserId())).orElse(false);
    }

    private static void renumber(List<Photo> ordered) {
        for (int i = 0; i < ordered.size(); i++) {
            ordered.get(i).moveTo(i);
        }
    }

    /** Trusts the file's first bytes, not the name or the type the phone claims. */
    static String detectImageType(MultipartFile file) {
        byte[] head = new byte[12];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(head, 0, head.length);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (read >= 3 && (head[0] & 0xFF) == 0xFF && (head[1] & 0xFF) == 0xD8 && (head[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (read >= 8 && Arrays.equals(Arrays.copyOf(head, 8),
                new byte[]{(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'})) {
            return "image/png";
        }
        if (read >= 12 && new String(head, 0, 4).equals("RIFF") && new String(head, 8, 4).equals("WEBP")) {
            return "image/webp";
        }
        if (read >= 12 && new String(head, 4, 4).equals("ftyp")) {
            String brand = new String(head, 8, 4);
            if (List.of("heic", "heix", "mif1", "msf1").contains(brand)) {
                return "image/heic";
            }
        }
        throw new InvalidRequestException("file", "upload a JPEG, PNG, WebP or HEIC photo");
    }

    record StoredPhoto(Resource resource, String contentType) {
    }
}
