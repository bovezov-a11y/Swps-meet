package com.meetswps.auth;

import com.meetswps.mail.EmailSender;
import com.meetswps.user.User;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

/**
 * Emails six-digit codes that students type into the app. A code is easier than a link on a phone, where the link
 * would open in a browser instead of the app. Only a hash is stored and each code allows a few guesses only.
 */
@Service
class EmailVerificationService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final VerificationCodeRepository codes;
    private final EmailSender emailSender;
    private final RegistrationProperties properties;

    EmailVerificationService(VerificationCodeRepository codes, EmailSender emailSender,
                             RegistrationProperties properties) {
        this.codes = codes;
        this.emailSender = emailSender;
        this.properties = properties;
    }

    /** Sends a new code unless one was sent moments ago, which stops the endpoint from being used to spam inboxes. */
    void sendCode(User user) {
        Instant now = Instant.now();
        Optional<VerificationCode> last = codes.findById(user.getId());
        if (last.isPresent() && last.get().getCreatedAt().plus(properties.resendCooldown()).isAfter(now)) {
            return;
        }

        String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        codes.save(new VerificationCode(user.getId(), hash(user, code), now, now.plus(properties.codeTtl())));
        emailSender.send(user.getEmail(), "Your Meet SWPS code: " + code, """
                Hi %s,

                Your Meet SWPS verification code is:

                    %s

                Type it into the app to finish creating your account. The code is valid for %d minutes.
                If you did not sign up, you can ignore this email.

                Meet SWPS is an unofficial project built by students and is not affiliated with SWPS University.
                """.formatted(user.getFirstName(), code, properties.codeTtl().toMinutes()));
    }

    /** True when the code is the user's current one; a wrong guess counts towards the attempt limit. */
    boolean check(User user, String code) {
        Optional<VerificationCode> found = codes.findById(user.getId());
        if (found.isEmpty() || found.get().isExpired(Instant.now())
                || found.get().getAttempts() >= properties.maxCodeAttempts()) {
            return false;
        }
        if (!MessageDigest.isEqual(found.get().getCodeHash().getBytes(StandardCharsets.UTF_8),
                hash(user, code).getBytes(StandardCharsets.UTF_8))) {
            found.get().recordFailedAttempt();
            return false;
        }
        codes.delete(found.get());
        return true;
    }

    private static String hash(User user, String code) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((user.getId() + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }
}
