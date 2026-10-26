package com.meetswps.notification;

import com.meetswps.auth.CurrentUser;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The app registers its push token after login and removes it on logout or when notifications are turned off. */
@RestController
class PushController {

    private final PushTokenRepository tokens;

    PushController(PushTokenRepository tokens) {
        this.tokens = tokens;
    }

    @PutMapping("/api/push-tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void register(CurrentUser user, @Valid @RequestBody PushTokenRequest request) {
        tokens.findById(request.token()).ifPresentOrElse(
                token -> token.assignTo(user.id()),
                () -> tokens.save(new PushToken(request.token(), user.id())));
    }

    @DeleteMapping("/api/push-tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void unregister(CurrentUser user, @Valid @RequestBody PushTokenRequest request) {
        tokens.findById(request.token()).filter(token -> token.getUserId().equals(user.id()))
                .ifPresent(tokens::delete);
    }

    record PushTokenRequest(
            @NotBlank @Pattern(regexp = "Expo(nent)?PushToken\\[[^\\]]{1,200}]", message = "is not an Expo push token")
            String token) {
    }
}
