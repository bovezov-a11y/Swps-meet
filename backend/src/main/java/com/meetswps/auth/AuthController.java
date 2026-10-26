package com.meetswps.auth;

import com.meetswps.auth.AuthDtos.AuthResponse;
import com.meetswps.auth.AuthDtos.LoginRequest;
import com.meetswps.auth.AuthDtos.RegisterRequest;
import com.meetswps.auth.AuthDtos.RegistrationResponse;
import com.meetswps.auth.AuthDtos.ResendVerificationRequest;
import com.meetswps.auth.AuthDtos.VerifyEmailRequest;
import jakarta.validation.Valid;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@EnableConfigurationProperties(RegistrationProperties.class)
class AuthController {

    private final AuthService authService;

    AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** Creates the account and emails a six-digit code; the user can log in once they enter it. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    RegistrationResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    /** Confirms the email address with the emailed code and logs the user in. */
    @PostMapping("/verify-email")
    AuthResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        return authService.verifyEmail(request.email(), request.code());
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.ACCEPTED)
    void resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.email());
    }

    @PostMapping("/login")
    AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
