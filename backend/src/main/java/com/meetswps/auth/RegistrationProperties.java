package com.meetswps.auth;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * @param allowedEmailDomains only addresses in these domains can register, e.g. {@code st.swps.edu.pl}
 * @param codeTtl             how long an emailed verification code stays valid
 * @param maxCodeAttempts     wrong guesses allowed before the code is discarded, so six digits cannot be brute-forced
 */
@Validated
@ConfigurationProperties("app.registration")
record RegistrationProperties(@NotEmpty List<String> allowedEmailDomains, Duration codeTtl, Duration resendCooldown,
                              int maxCodeAttempts) {
}
