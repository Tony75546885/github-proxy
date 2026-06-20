package com.example.githubproxy;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class GithubProxyIntegrationTest {

    @RegisterExtension
    static final WireMockExtension GITHUB = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void overrideGithubBaseUrl(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", GITHUB::baseUrl);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnOnlyNonForkRepositoriesWithBranchesForExistingUser() {
        GITHUB.stubFor(get(urlEqualTo("/users/octocat/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "hello-world",
                                    "owner": {"login": "octocat"},
                                    "fork": false
                                  },
                                  {
                                    "name": "forked-project",
                                    "owner": {"login": "octocat"},
                                    "fork": true
                                  }
                                ]
                                """)));

        GITHUB.stubFor(get(urlEqualTo("/repos/octocat/hello-world/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {"name": "main", "commit": {"sha": "abc123"}},
                                  {"name": "dev",  "commit": {"sha": "def456"}}
                                ]
                                """)));

        ResponseEntity<RepositoryResponse[]> response = restTemplate.getForEntity(
                "/users/octocat/repositories", RepositoryResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(
                new RepositoryResponse(
                        "hello-world",
                        "octocat",
                        java.util.List.of(
                                new BranchResponse("main", "abc123"),
                                new BranchResponse("dev", "def456"))));

        GITHUB.verify(0, getRequestedFor(urlEqualTo("/repos/octocat/forked-project/branches")));
    }

    @Test
    void shouldReturnNotFoundWithErrorPayloadWhenUserDoesNotExist() {
        GITHUB.stubFor(get(urlEqualTo("/users/ghost/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"message\":\"Not Found\"}")));

        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/users/ghost/repositories", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().message()).contains("ghost");
    }
}
