package com.meetswps.auth;

import com.jayway.jsonpath.JsonPath;
import com.meetswps.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthControllerTest extends ApiTest {

    private static String uniqueEmail() {
        return "student-" + UUID.randomUUID() + "@st.swps.edu.pl";
    }

    @Test
    void registerOnlyAcceptsUniversityEmails() {
        MvcTestResult result = register("anna@gmail.com");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.errors.email").asString().contains("st.swps.edu.pl");
    }

    @Test
    void theEmailedCodeLogsTheUserInOnce() {
        String email = uniqueEmail();
        assertThat(register(email)).hasStatus(HttpStatus.ACCEPTED);
        String code = verificationCode(email);

        MvcTestResult verified = verifyEmail(email, code);
        assertThat(verified).hasStatus(HttpStatus.OK);
        assertThat(verified).bodyJson().extractingPath("$.user.email").isEqualTo(email);
        assertThat(verified).bodyJson().extractingPath("$.token").asString().isNotBlank();

        assertThat(verifyEmail(email, code)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void aCodeStopsWorkingAfterTooManyWrongGuesses() {
        String email = uniqueEmail();
        register(email);
        String code = verificationCode(email);
        String wrong = code.equals("000000") ? "111111" : "000000";

        for (int i = 0; i < 5; i++) {
            MvcTestResult result = verifyEmail(email, wrong);
            assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
            assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INVALID_VERIFICATION_CODE");
        }

        assertThat(verifyEmail(email, code)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void unverifiedUsersCannotLogInButLearnWhyOnlyWithTheRightPassword() {
        String email = uniqueEmail();
        register(email);

        MvcTestResult login = login(email, PASSWORD);
        assertThat(login).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(login).bodyJson().extractingPath("$.code").isEqualTo("EMAIL_NOT_VERIFIED");
        assertThat(login(email, "wrong-password")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void registerRejectsAnEmailThatIsAlreadyVerified() {
        assertThat(register(registerUser().email())).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void resendingNeverRevealsWhetherAnAccountExists() {
        MvcTestResult result = mvc.post().uri("/api/auth/resend-verification")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s"}
                        """.formatted(uniqueEmail()))
                .exchange();

        assertThat(result).hasStatus(HttpStatus.ACCEPTED);
    }

    @Test
    void loginReturnsTokenThatAuthenticatesRequests() throws Exception {
        TestUser user = registerUser();

        MvcTestResult login = login(user.email(), PASSWORD);
        assertThat(login).hasStatus(HttpStatus.OK);
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");

        MvcTestResult account = mvc.get().uri("/api/account").header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .exchange();
        assertThat(account).hasStatus(HttpStatus.OK);
        assertThat(account).bodyJson().extractingPath("$.user.email").isEqualTo(user.email());
        assertThat(account).bodyJson().extractingPath("$.hasProfile").isEqualTo(false);
    }

    @Test
    void protectedEndpointRequiresToken() {
        assertThat(mvc.get().uri("/api/account")).hasStatus(HttpStatus.UNAUTHORIZED);
    }

    private MvcTestResult login(String email, String password) {
        return mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password))
                .exchange();
    }
}
