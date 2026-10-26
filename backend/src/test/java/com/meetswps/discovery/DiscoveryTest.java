package com.meetswps.discovery;

import com.meetswps.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryTest extends ApiTest {

    @Test
    void eachSectionListsOnlyStudentsWhoChoseIt() {
        String program = uniqueProgram();
        TestUser viewer = student(program, "[\"FRIENDS\", \"INTERNSHIP\"]");
        TestUser friend = student(program, "[\"FRIENDS\"]");
        TestUser mentor = student(program, "[\"INTERNSHIP\"]");
        student(program, "[\"SOCIAL\"]");

        MvcTestResult friends = discover(viewer, "FRIENDS", program);
        assertThat(friends).hasStatus(HttpStatus.OK);
        assertThat(friends).bodyJson().extractingPath("$[*].userId").asArray().containsExactly(friend.id());
        // Internship details are only shown within the internship section.
        assertThat(friends).bodyJson().extractingPath("$[0].industry").isNull();
        assertThat(friends).bodyJson().extractingPath("$[0].instagram").isNull();

        MvcTestResult internship = discover(viewer, "INTERNSHIP", program);
        assertThat(internship).bodyJson().extractingPath("$[*].userId").asArray().containsExactly(mentor.id());
        assertThat(internship).bodyJson().extractingPath("$[0].industry").isEqualTo("Marketing");
    }

    @Test
    void aSectionMustBeChosenBeforeBrowsingIt() {
        TestUser viewer = student(uniqueProgram(), "[\"FRIENDS\"]");

        assertThat(get(viewer, "/api/discover?intent=DATING")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void datingShowsOnlyMutuallyCompatibleStudents() {
        String program = uniqueProgram();
        TestUser woman = dater(program, "WOMAN", "[\"MAN\"]");
        TestUser man = dater(program, "MAN", "[\"WOMAN\"]");
        dater(program, "MAN", "[\"MAN\"]");
        dater(program, "WOMAN", "[\"MAN\"]");

        assertThat(discover(woman, "DATING", program)).bodyJson().extractingPath("$[*].userId").asArray()
                .containsExactly(man.id());
        // In the friends section everyone is listed, but the shared sections never reveal dating to others.
        assertThat(discover(woman, "FRIENDS", program)).bodyJson().extractingPath("$[*].sharedIntents").asArray()
                .hasSize(3);
    }

    @Test
    void blockedStudentsDisappearForBoth() {
        String program = uniqueProgram();
        TestUser viewer = student(program, "[\"FRIENDS\"]");
        TestUser other = student(program, "[\"FRIENDS\"]");

        assertThat(put(viewer, "/api/blocks/" + other.id())).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(discover(viewer, "FRIENDS", program)).bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(discover(other, "FRIENDS", program)).bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(get(other, "/api/profiles/" + viewer.id())).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void filtersNarrowTheList() {
        String program = uniqueProgram();
        TestUser viewer = student(program, "[\"SOCIAL\"]");
        TestUser climber = student(program, "[\"SOCIAL\"]");

        assertThat(get(viewer, "/api/discover?intent=SOCIAL&interest=climbing&program=" + program))
                .bodyJson().extractingPath("$[*].userId").asArray().containsExactly(climber.id());
        assertThat(get(viewer, "/api/discover?intent=SOCIAL&interest=chess&program=" + program))
                .bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(get(viewer, "/api/discover?intent=SOCIAL&campus=SOPOT&program=" + program))
                .bodyJson().extractingPath("$").asArray().isEmpty();
    }
}
