package com.pretz.geographic.infrastructure.adapter.in.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MockMvcTester.MockMvcRequestBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

/**
 * Verifies the authorization matrix: every endpoint against every role. Request bodies are intentionally invalid,
 * so permitted requests fail validation instead of modifying data; only 401/403 responses matter here.
 */
class SecurityIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private MockMvcTester mockMvc;

    enum Role {
        USER_READ, USER_WRITE, ADMIN
    }

    record Endpoint(HttpMethod method, String uri, Role requiredRole) {

        @Override
        public String toString() {
            return method + " " + uri;
        }
    }

    private static final List<Endpoint> ENDPOINTS = List.of(
            new Endpoint(GET, "/api/games", Role.USER_READ),
            new Endpoint(GET, "/api/games/1", Role.USER_READ),
            new Endpoint(GET, "/api/players", Role.USER_READ),
            new Endpoint(GET, "/api/players/1", Role.USER_READ),
            new Endpoint(GET, "/api/daily-ranking?date=2026-09-01", Role.USER_READ),
            new Endpoint(GET, "/api/reports/daily-rankings?from=2026-09-01&to=2026-09-30", Role.USER_READ),
            new Endpoint(GET, "/api/reports/daily-rankings/games/1?from=2026-09-01&to=2026-09-30", Role.USER_READ),
            new Endpoint(GET, "/api/reports/weekly-rankings?fromYear=2026&fromWeek=30&toYear=2026&toWeek=40", Role.USER_READ),
            new Endpoint(GET, "/api/reports/weekly-rankings/games/1?fromYear=2026&fromWeek=30&toYear=2026&toWeek=40", Role.USER_READ),
            new Endpoint(GET, "/api/weekly-ranking?year=2026&week=37", Role.USER_WRITE),
            new Endpoint(POST, "/api/daily-entries", Role.USER_WRITE),
            new Endpoint(POST, "/api/games", Role.ADMIN),
            new Endpoint(POST, "/api/players", Role.ADMIN));

    static Stream<Arguments> endpointsAndRoles() {
        return ENDPOINTS.stream()
                .flatMap(endpoint -> Arrays.stream(Role.values()).map(role -> Arguments.of(endpoint, role)));
    }

    static Stream<Endpoint> endpoints() {
        return ENDPOINTS.stream();
    }

    @ParameterizedTest(name = "{0} as {1}")
    @MethodSource("endpointsAndRoles")
    void shouldAuthorizeRoleOnlyWhenItIncludesRequiredRole(Endpoint endpoint, Role role) {
        //given
        var permitted = role.ordinal() >= endpoint.requiredRole().ordinal();
        //when
        var result = request(endpoint).with(user("user").roles(role.name())).exchange();
        //then
        if (permitted) {
            assertThat(result.getResponse().getStatus())
                    .isNotIn(HttpStatus.UNAUTHORIZED.value(), HttpStatus.FORBIDDEN.value());
        } else {
            assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void shouldRequireAuthentication(Endpoint endpoint) {

        assertThat(request(endpoint))
                .hasStatus(HttpStatus.UNAUTHORIZED)
                .containsHeader("WWW-Authenticate");
    }

    @Test
    void shouldDenyRequestsOutsideOfKnownEndpoints() {

        assertThat(mockMvc.delete().uri("/api/games/1").with(user("admin").roles(Role.ADMIN.name())))
                .hasStatus(HttpStatus.FORBIDDEN);
    }

    private MockMvcRequestBuilder request(Endpoint endpoint) {
        var request = mockMvc.method(endpoint.method()).uri(endpoint.uri());
        return endpoint.method() == POST
                ? request.contentType(MediaType.APPLICATION_JSON).content("{}")
                : request;
    }
}
