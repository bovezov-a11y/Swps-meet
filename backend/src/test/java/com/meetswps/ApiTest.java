package com.meetswps;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for API tests: a full application context on a real PostgreSQL, plus helpers for authenticated calls.
 * Tests share one database, so they create their own users and filter by unique program names instead of relying
 * on a clean state.
 */
@SpringBootTest(properties = {"app.storage.local-dir=target/test-uploads",
        "app.roles.admin-emails=" + ApiTest.ADMIN_EMAIL})
@AutoConfigureMockMvc
@Import({TestcontainersConfiguration.class, TestDoubles.class})
public abstract class ApiTest {

    protected static final String ADMIN_EMAIL = "staff-admin@swps.edu.pl";
    protected static final String PASSWORD = "correct-horse";
    protected static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10, 'J', 'F', 'I', 'F'};

    private static final Pattern CODE = Pattern.compile("\\b(\\d{6})\\b");

    @Autowired
    protected MockMvcTester mvc;

    @Autowired
    protected TestDoubles.RecordingEmailSender emails;

    @Autowired
    protected TestDoubles.RecordingPushSender pushes;

    protected record TestUser(String id, String email, String token) {
    }

    /** Registers a student and confirms their email with the code they were sent, the way a real user would. */
    protected TestUser registerUser() {
        return registerUser("student-" + UUID.randomUUID() + "@st.swps.edu.pl");
    }

    protected TestUser registerUser(String email) {
        register(email);
        String body = body(verifyEmail(email, verificationCode(email)));
        return new TestUser(JsonPath.read(body, "$.user.id"), email, JsonPath.read(body, "$.token"));
    }

    protected MvcTestResult register(String email) {
        return mvc.post().uri("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s", "firstName": "Ola", "privacyPolicyVersion": "1.1"}
                        """.formatted(email, PASSWORD))
                .exchange();
    }

    protected TestUser login(TestUser user) {
        MvcTestResult result = mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(user.email(), PASSWORD))
                .exchange();
        return new TestUser(user.id(), user.email(), JsonPath.read(body(result), "$.token"));
    }

    /** The configured admin account; tests share one database, so it is created once and reused. */
    protected TestUser admin() {
        MvcTestResult result = mvc.post().uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(ADMIN_EMAIL, PASSWORD))
                .exchange();
        if (result.getResponse().getStatus() == 200) {
            String body = body(result);
            return new TestUser(JsonPath.read(body, "$.user.id"), ADMIN_EMAIL, JsonPath.read(body, "$.token"));
        }
        return registerUser(ADMIN_EMAIL);
    }

    protected TestUser moderator() {
        TestUser user = registerUser();
        post(admin(), "/api/admin/moderators", """
                {"email": "%s"}
                """.formatted(user.email()));
        return login(user);
    }

    protected String verificationCode(String email) {
        String message = emails.lastEmailTo(email).orElseThrow(() -> new AssertionError("No email sent to " + email));
        Matcher matcher = CODE.matcher(message);
        if (!matcher.find()) {
            throw new AssertionError("No code in: " + message);
        }
        return matcher.group(1);
    }

    protected MvcTestResult verifyEmail(String email, String code) {
        return mvc.post().uri("/api/auth/verify-email")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "code": "%s"}
                        """.formatted(email, code))
                .exchange();
    }

    /** A student with a finished profile in the given sections, studying {@code program}. */
    protected TestUser student(String program, String intentsJson) {
        TestUser user = registerUser();
        MvcTestResult result = saveProfile(user, profileJson(program, intentsJson));
        if (result.getResponse().getStatus() != 200) {
            throw new AssertionError("Profile not saved: " + body(result));
        }
        return user;
    }

    protected MvcTestResult saveProfile(TestUser user, String json) {
        return put(user, "/api/profile", json);
    }

    protected static String profileJson(String program, String intentsJson) {
        return """
                {"firstName": "Ola", "birthDate": "%s", "campus": "WARSAW", "program": "%s", "studyYear": 2,
                 "degreeLevel": "BACHELOR", "bio": "Hi!", "interests": ["Climbing", "Jazz"], "languages": ["Polish"],
                 "instagram": "https://www.instagram.com/ola.k/", "intents": %s, "internshipRole": "LOOKING",
                 "industry": "Marketing"}
                """.formatted(LocalDate.now().minusYears(21), program, intentsJson);
    }

    protected static String uniqueProgram() {
        return "Program-" + UUID.randomUUID();
    }

    protected MvcTestResult uploadPhoto(TestUser user, byte[] content) {
        return mvc.post().uri("/api/profile/photos")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                .multipart().file(new MockMultipartFile("file", "photo.jpg", "image/jpeg", content))
                .exchange();
    }

    protected MvcTestResult get(TestUser user, String uri) {
        return mvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()).exchange();
    }

    protected MvcTestResult post(TestUser user, String uri, String json) {
        return mvc.post().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                .contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    protected MvcTestResult post(TestUser user, String uri) {
        return mvc.post().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()).exchange();
    }

    protected MvcTestResult put(TestUser user, String uri, String json) {
        return mvc.put().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                .contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    protected MvcTestResult put(TestUser user, String uri) {
        return mvc.put().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()).exchange();
    }

    protected MvcTestResult delete(TestUser user, String uri) {
        return mvc.delete().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()).exchange();
    }

    protected MvcTestResult delete(TestUser user, String uri, String json) {
        return mvc.delete().uri(uri).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                .contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    /** Discover a section, filtered to one program so other tests' students are left out. */
    protected MvcTestResult discover(TestUser user, String intent, String program) {
        return get(user, "/api/discover?intent=" + intent + "&program=" + program);
    }

    protected static String body(MvcTestResult result) {
        try {
            return result.getResponse().getContentAsString();
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
