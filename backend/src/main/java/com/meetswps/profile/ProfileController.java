package com.meetswps.profile;

import com.meetswps.auth.CurrentUser;
import com.meetswps.profile.ProfileDtos.MyProfileResponse;
import com.meetswps.profile.ProfileDtos.PhotoResponse;
import com.meetswps.profile.ProfileDtos.ReorderPhotosRequest;
import com.meetswps.profile.ProfileDtos.UpdateProfileRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@RestController
class ProfileController {

    private final ProfileService profileService;
    private final PhotoService photoService;

    ProfileController(ProfileService profileService, PhotoService photoService) {
        this.profileService = profileService;
        this.photoService = photoService;
    }

    @GetMapping("/api/profile")
    MyProfileResponse myProfile(CurrentUser user) {
        return profileService.get(user.id());
    }

    @PutMapping("/api/profile")
    MyProfileResponse saveProfile(CurrentUser user, @Valid @RequestBody UpdateProfileRequest request) {
        return profileService.save(user.id(), request);
    }

    @PostMapping(path = "/api/profile/photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    PhotoResponse uploadPhoto(CurrentUser user, @RequestParam("file") MultipartFile file) {
        return photoService.upload(user.id(), file);
    }

    @PutMapping("/api/profile/photos/order")
    List<PhotoResponse> reorderPhotos(CurrentUser user, @Valid @RequestBody ReorderPhotosRequest request) {
        return photoService.reorder(user.id(), request.photoIds());
    }

    @DeleteMapping("/api/profile/photos/{photoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deletePhoto(CurrentUser user, @PathVariable UUID photoId) {
        photoService.delete(user.id(), photoId);
    }

    @GetMapping("/api/photos/{photoId}")
    ResponseEntity<Resource> photo(CurrentUser user, @PathVariable UUID photoId) {
        PhotoService.StoredPhoto photo = photoService.load(user.id(), photoId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.contentType()))
                // Photo ids never change content, but access can be revoked by a block, so caches stay private.
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePrivate())
                .body(photo.resource());
    }
}
