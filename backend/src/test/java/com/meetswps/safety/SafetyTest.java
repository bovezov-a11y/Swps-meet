package com.meetswps.safety;

import com.jayway.jsonpath.JsonPath;
import com.meetswps.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SafetyTest extends ApiTest {

    @Test
    void blockingEndsAMatch() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");
        put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");
        put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS");

        put(ola, "/api/blocks/" + kuba.id());

        assertThat(get(kuba, "/api/matches")).bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(get(ola, "/api/blocks")).bodyJson().extractingPath("$[0].userId").isEqualTo(kuba.id());
    }

    @Test
    void enoughReportsHideAProfileUntilAModeratorRestoresIt() {
        String program = uniqueProgram();
        TestUser target = student(program, "[\"SOCIAL\"]");
        TestUser bystander = student(program, "[\"SOCIAL\"]");

        for (int i = 0; i < 3; i++) {
            TestUser reporter = student(program, "[\"SOCIAL\"]");
            assertThat(post(reporter, "/api/reports", """
                    {"userId": "%s", "reason": "FAKE_PROFILE", "details": "Not a student"}
                    """.formatted(target.id()))).hasStatus(HttpStatus.CREATED);
        }

        assertThat(discover(bystander, "SOCIAL", program)).bodyJson().extractingPath("$[*].userId").asArray()
                .doesNotContain(target.id());

        TestUser moderator = moderator();
        List<String> queued = JsonPath.read(body(get(moderator, "/api/moderation/reports")), "$[*].userId");
        assertThat(queued).contains(target.id());
        assertThat(get(bystander, "/api/moderation/reports")).hasStatus(HttpStatus.FORBIDDEN);

        assertThat(post(moderator, "/api/moderation/users/" + target.id() + "/restore"))
                .hasStatus(HttpStatus.NO_CONTENT);
        assertThat(discover(bystander, "SOCIAL", program)).bodyJson().extractingPath("$[*].userId").asArray()
                .contains(target.id());
    }

    @Test
    void reportingTwiceIsRejected() {
        String program = uniqueProgram();
        TestUser target = student(program, "[\"SOCIAL\"]");
        TestUser reporter = student(program, "[\"SOCIAL\"]");
        String json = """
                {"userId": "%s", "reason": "SPAM"}
                """.formatted(target.id());

        assertThat(post(reporter, "/api/reports", json)).hasStatus(HttpStatus.CREATED);
        assertThat(post(reporter, "/api/reports", json)).hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void aBannedStudentIsLoggedOutAndCannotLogInAgain() {
        TestUser target = student(uniqueProgram(), "[\"SOCIAL\"]");

        assertThat(post(moderator(), "/api/moderation/users/" + target.id() + "/ban"))
                .hasStatus(HttpStatus.NO_CONTENT);

        assertThat(get(target, "/api/account")).hasStatus(HttpStatus.UNAUTHORIZED);
        assertThat(mvc.post().uri("/api/auth/login").contentType("application/json").content("""
                {"email": "%s", "password": "%s"}
                """.formatted(target.email(), PASSWORD))).hasStatus(HttpStatus.FORBIDDEN)
                .bodyJson().extractingPath("$.code").isEqualTo("ACCOUNT_SUSPENDED");
    }

    @Test
    void deletingTheAccountRemovesEverything() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");
        uploadPhoto(ola, JPEG);
        put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");
        put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS");

        assertThat(delete(ola, "/api/account", """
                {"password": "wrong"}
                """)).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(delete(ola, "/api/account", """
                {"password": "%s"}
                """.formatted(PASSWORD))).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(get(kuba, "/api/matches")).bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(discover(kuba, "FRIENDS", program)).bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(get(ola, "/api/account")).hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
