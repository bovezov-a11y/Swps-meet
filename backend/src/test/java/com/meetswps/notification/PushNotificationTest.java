package com.meetswps.notification;

import com.jayway.jsonpath.JsonPath;
import com.meetswps.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class PushNotificationTest extends ApiTest {

    @Autowired
    private PushTokenRepository tokens;

    private static String newToken() {
        return "ExponentPushToken[" + UUID.randomUUID() + "]";
    }

    private void registerToken(TestUser user, String token) {
        assertThat(put(user, "/api/push-tokens", """
                {"token": "%s"}
                """.formatted(token))).hasStatus(HttpStatus.NO_CONTENT);
    }

    /** Notifications are sent in the background, after the request has finished. */
    private static <T> T eventually(Supplier<List<T>> found) {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline) {
            List<T> items = found.get();
            if (!items.isEmpty()) {
                return items.getLast();
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        throw new AssertionError("Nothing was sent in time");
    }

    @Test
    void bothStudentsAreToldAboutANewMatchAndTheRecipientAboutAMessage() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");
        String olaPhone = newToken();
        String kubaPhone = newToken();
        registerToken(ola, olaPhone);
        registerToken(kuba, kubaPhone);

        put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");
        String matchId = JsonPath.read(body(put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS")), "$.matchId");

        PushMessage toOla = eventually(() -> pushes.sentTo(olaPhone));
        assertThat(toOla.title()).contains("match");
        assertThat(toOla.data()).containsEntry("url", "/chat/" + matchId);
        assertThat(eventually(() -> pushes.sentTo(kubaPhone)).title()).contains("match");

        post(ola, "/api/matches/" + matchId + "/messages", """
                {"body": "Coffee after class?"}
                """);
        PushMessage message = eventually(() -> pushes.sentTo(kubaPhone).stream()
                .filter(m -> m.body().equals("Coffee after class?")).toList());
        assertThat(message.title()).isEqualTo("Ola");
        assertThat(pushes.sentTo(olaPhone)).noneMatch(m -> m.body().equals("Coffee after class?"));
    }

    @Test
    void aLikeAloneSendsNothing() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");
        String kubaPhone = newToken();
        registerToken(kuba, kubaPhone);

        put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");

        assertThat(pushes.sentTo(kubaPhone)).isEmpty();
    }

    @Test
    void aPhoneBelongsToWhoeverLoggedInLast() {
        TestUser first = registerUser();
        TestUser second = registerUser();
        String phone = newToken();

        registerToken(first, phone);
        registerToken(second, phone);

        assertThat(tokens.findById(phone)).get().extracting(PushToken::getUserId)
                .isEqualTo(UUID.fromString(second.id()));
        // Logging out on the phone removes it, but only for the account that owns it.
        delete(first, "/api/push-tokens", """
                {"token": "%s"}
                """.formatted(phone));
        assertThat(tokens.existsById(phone)).isTrue();
        delete(second, "/api/push-tokens", """
                {"token": "%s"}
                """.formatted(phone));
        assertThat(tokens.existsById(phone)).isFalse();
    }

    @Test
    void tokensOfUninstalledAppsAreForgotten() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");
        String oldPhone = newToken();
        registerToken(kuba, oldPhone);
        pushes.unregistered.add(oldPhone);

        put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");
        put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS");

        eventually(() -> pushes.sentTo(oldPhone));
        eventually(() -> tokens.existsById(oldPhone) ? List.of() : List.of(true));
    }

    @Test
    void onlyExpoTokensAreAccepted() {
        assertThat(put(registerUser(), "/api/push-tokens", """
                {"token": "not-a-token"}
                """)).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void longMessagesAreShortenedInTheNotification() {
        assertThat(PushNotifications.preview("a".repeat(300))).hasSize(PushNotifications.PREVIEW_LENGTH).endsWith("…");
        assertThat(PushNotifications.preview("Hi")).isEqualTo("Hi");
    }
}
