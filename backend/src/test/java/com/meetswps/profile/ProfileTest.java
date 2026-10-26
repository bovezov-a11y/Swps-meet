package com.meetswps.profile;

import com.jayway.jsonpath.JsonPath;
import com.meetswps.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProfileTest extends ApiTest {

    @Test
    void savingAProfileFinishesOnboardingAndNormalisesInput() {
        TestUser user = registerUser();
        assertThat(get(user, "/api/profile")).hasStatus(HttpStatus.NOT_FOUND);

        MvcTestResult saved = saveProfile(user, profileJson(uniqueProgram(), "[\"FRIENDS\", \"INTERNSHIP\"]"));

        assertThat(saved).hasStatus(HttpStatus.OK);
        assertThat(saved).bodyJson().extractingPath("$.instagram").isEqualTo("ola.k");
        assertThat(saved).bodyJson().extractingPath("$.age").isEqualTo(21);
        assertThat(saved).bodyJson().extractingPath("$.internshipRole").isEqualTo("LOOKING");
        assertThat(get(user, "/api/account")).bodyJson().extractingPath("$.hasProfile").isEqualTo(true);
    }

    @Test
    void studentsMustBeAdults() {
        TestUser user = registerUser();
        String json = profileJson(uniqueProgram(), "[\"FRIENDS\"]")
                .replaceFirst("\\d{4}-\\d{2}-\\d{2}", LocalDate.now().minusYears(17).toString());

        MvcTestResult result = saveProfile(user, json);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.errors.birthDate").isNotNull();
    }

    @Test
    void theRemovedDatingSectionCannotBeChosen() {
        TestUser user = registerUser();

        assertThat(saveProfile(user, profileJson(uniqueProgram(), "[\"DATING\"]")))
                .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void anInvalidInstagramIsRejected() {
        TestUser user = registerUser();
        String json = profileJson(uniqueProgram(), "[\"FRIENDS\"]")
                .replace("https://www.instagram.com/ola.k/", "not a username!");

        assertThat(saveProfile(user, json)).hasStatus(HttpStatus.BAD_REQUEST)
                .bodyJson().extractingPath("$.errors.instagram").isNotNull();
    }

    @Test
    void photosAreLimitedCheckedAndReorderable() {
        TestUser user = student(uniqueProgram(), "[\"FRIENDS\"]");

        assertThat(uploadPhoto(user, "not an image".getBytes())).hasStatus(HttpStatus.BAD_REQUEST);
        String first = JsonPath.read(body(uploadPhoto(user, JPEG)), "$.id");
        String second = JsonPath.read(body(uploadPhoto(user, JPEG)), "$.id");
        assertThat(uploadPhoto(user, JPEG)).hasStatus(HttpStatus.CREATED);
        assertThat(uploadPhoto(user, JPEG)).hasStatus(HttpStatus.BAD_REQUEST);

        List<String> ids = JsonPath.read(body(get(user, "/api/profile")), "$.photos[*].id");
        List<String> reversed = ids.reversed();
        assertThat(put(user, "/api/profile/photos/order", """
                {"photoIds": ["%s", "%s", "%s"]}
                """.formatted(reversed.get(0), reversed.get(1), reversed.get(2)))).hasStatus(HttpStatus.OK);
        assertThat((List<String>) JsonPath.read(body(get(user, "/api/profile")), "$.photos[*].id"))
                .containsExactlyElementsOf(reversed);

        assertThat(delete(user, "/api/profile/photos/" + first)).hasStatus(HttpStatus.NO_CONTENT);
        assertThat((List<String>) JsonPath.read(body(get(user, "/api/profile")), "$.photos[*].id"))
                .hasSize(2).contains(second);
        assertThat(get(user, "/api/photos/" + second)).hasStatus(HttpStatus.OK).hasContentType("image/jpeg");
    }

    @Test
    void photosAreOnlyVisibleToStudentsWhoShareASection() {
        String program = uniqueProgram();
        TestUser owner = student(program, "[\"FRIENDS\"]");
        TestUser friend = student(program, "[\"FRIENDS\"]");
        TestUser other = student(program, "[\"SOCIAL\"]");
        String photo = JsonPath.read(body(uploadPhoto(owner, JPEG)), "$.id");

        assertThat(get(friend, "/api/photos/" + photo)).hasStatus(HttpStatus.OK);
        assertThat(get(other, "/api/photos/" + photo)).hasStatus(HttpStatus.NOT_FOUND);
    }
}
