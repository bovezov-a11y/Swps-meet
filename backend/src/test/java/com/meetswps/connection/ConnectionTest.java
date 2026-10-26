package com.meetswps.connection;

import com.jayway.jsonpath.JsonPath;
import com.meetswps.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;

class ConnectionTest extends ApiTest {

    @Test
    void twoLikesMakeAMatchThatRevealsInstagramAndAllowsChat() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");

        MvcTestResult first = put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");
        assertThat(first).hasStatus(HttpStatus.OK).bodyJson().extractingPath("$.matched").isEqualTo(false);
        assertThat(get(kuba, "/api/profiles/" + ola.id())).bodyJson().extractingPath("$.instagram").isNull();
        assertThat(get(ola, "/api/matches")).bodyJson().extractingPath("$").asArray().isEmpty();

        MvcTestResult second = put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS");
        assertThat(second).bodyJson().extractingPath("$.matched").isEqualTo(true);
        String matchId = JsonPath.read(body(second), "$.matchId");

        assertThat(get(kuba, "/api/profiles/" + ola.id())).bodyJson().extractingPath("$.instagram")
                .isEqualTo("ola.k");
        assertThat(get(ola, "/api/matches")).bodyJson().extractingPath("$[0].id").isEqualTo(matchId);

        assertThat(post(ola, "/api/matches/" + matchId + "/messages", """
                {"body": "  Coffee after class?  "}
                """)).hasStatus(HttpStatus.CREATED);
        MvcTestResult messages = get(kuba, "/api/matches/" + matchId + "/messages");
        assertThat(messages).bodyJson().extractingPath("$[0].body").isEqualTo("Coffee after class?");
        assertThat(messages).bodyJson().extractingPath("$[0].mine").isEqualTo(false);
        assertThat(get(kuba, "/api/matches")).bodyJson().extractingPath("$[0].lastMessage.body")
                .isEqualTo("Coffee after class?");
    }

    @Test
    void likesOnlyCountInASharedSection() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\", \"SOCIAL\"]");

        assertThat(put(ola, "/api/likes/" + kuba.id() + "?intent=SOCIAL")).hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void strangersCannotReadOrWriteAMatch() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");
        TestUser stranger = student(program, "[\"FRIENDS\"]");
        put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");
        String matchId = JsonPath.read(body(put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS")), "$.matchId");

        assertThat(get(stranger, "/api/matches/" + matchId + "/messages")).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(post(stranger, "/api/matches/" + matchId + "/messages", """
                {"body": "hi"}
                """)).hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void unmatchingEndsTheChat() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kuba = student(program, "[\"FRIENDS\"]");
        put(ola, "/api/likes/" + kuba.id() + "?intent=FRIENDS");
        String matchId = JsonPath.read(body(put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS")), "$.matchId");

        assertThat(delete(ola, "/api/matches/" + matchId)).hasStatus(HttpStatus.NO_CONTENT);

        assertThat(get(kuba, "/api/matches")).bodyJson().extractingPath("$").asArray().isEmpty();
        assertThat(get(kuba, "/api/matches/" + matchId + "/messages")).hasStatus(HttpStatus.NOT_FOUND);
        // Likes were cleared, so one new like does not match again.
        assertThat(put(kuba, "/api/likes/" + ola.id() + "?intent=FRIENDS")).bodyJson()
                .extractingPath("$.matched").isEqualTo(false);
    }
}
