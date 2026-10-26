package com.meetswps.account;

import com.meetswps.auth.CurrentUser;
import com.meetswps.common.InvalidRequestException;
import com.meetswps.common.NotFoundException;
import com.meetswps.profile.Photo;
import com.meetswps.profile.PhotoRepository;
import com.meetswps.profile.ProfileRepository;
import com.meetswps.storage.FileStorage;
import com.meetswps.user.User;
import com.meetswps.user.UserRepository;
import com.meetswps.user.UserResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
class AccountController {

    private final UserRepository users;
    private final ProfileRepository profiles;
    private final PhotoRepository photos;
    private final FileStorage storage;
    private final PasswordEncoder passwordEncoder;

    AccountController(UserRepository users, ProfileRepository profiles, PhotoRepository photos, FileStorage storage,
                      PasswordEncoder passwordEncoder) {
        this.users = users;
        this.profiles = profiles;
        this.photos = photos;
        this.storage = storage;
        this.passwordEncoder = passwordEncoder;
    }

    /** The account, and whether onboarding is finished so the app knows which screen to open. */
    @GetMapping("/api/account")
    @Transactional(readOnly = true)
    AccountResponse account(CurrentUser currentUser) {
        User user = users.findById(currentUser.id()).orElseThrow(() -> new NotFoundException("User not found"));
        return new AccountResponse(UserResponse.from(user), profiles.existsById(user.getId()));
    }

    /**
     * Deletes the account and everything that belongs to it: profile, photos, likes, matches, messages and blocks.
     * Asks for the password so a borrowed, unlocked phone cannot delete someone's account.
     */
    @DeleteMapping("/api/account")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void delete(CurrentUser currentUser, @Valid @RequestBody DeleteAccountRequest request) {
        User user = users.findById(currentUser.id()).orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidRequestException("password", "the password is not correct");
        }
        List<Photo> userPhotos = photos.findAllByUserIdOrderByPosition(user.getId());
        // Rows referencing the user are removed by the database (on delete cascade).
        users.delete(user);
        users.flush();
        userPhotos.forEach(photo -> storage.delete(photo.storageKey()));
    }

    record AccountResponse(UserResponse user, boolean hasProfile) {
    }

    record DeleteAccountRequest(@NotBlank String password) {
    }
}
