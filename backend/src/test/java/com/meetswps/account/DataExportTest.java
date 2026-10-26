package com.meetswps.account;

import com.jayway.jsonpath.JsonPath;
import com.meetswps.ApiTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataExportTest extends ApiTest {

    @Test
    void studentsCanDownloadEverythingStoredAboutThem() {
        String program = uniqueProgram();
        TestUser ola = student(program, "[\"FRIENDS\"]");
        TestUser kasia = student(program, "[\"FRIENDS\"]");
        put(ola, "/api/likes/" + kasia.id() + "?intent=FRIENDS");
        String matchId = JsonPath.read(body(put(kasia, "/api/likes/" + ola.id() + "?intent=FRIENDS")), "$.matchId");
        post(ola, "/api/matches/" + matchId + "/messages", """
                {"body": "Coffee after class?"}
                """);

        MvcTestResult export = get(ola, "/api/account/export");

        assertThat(export).hasStatus(HttpStatus.OK);
        assertThat(export.getResponse().getHeader("Content-Disposition")).contains("meet-swps-data.json");
        String json = body(export);
        assertThat((String) JsonPath.read(json, "$.account.email")).isEqualTo(ola.email());
        assertThat((String) JsonPath.read(json, "$.profile.program")).isEqualTo(program);
        assertThat((List<?>) JsonPath.read(json, "$.likesSent")).hasSize(1);
        assertThat((List<?>) JsonPath.read(json, "$.matches")).hasSize(1);
        assertThat((String) JsonPath.read(json, "$.messages[0].body")).isEqualTo("Coffee after class?");
        assertThat(json).doesNotContain("password");
    }

    @Test
    void theExportNeedsASignedInStudent() {
        assertThat(mvc.get().uri("/api/account/export").exchange()).hasStatus(HttpStatus.UNAUTHORIZED);
    }
}
