package com.example.githubproxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
final class GithubClient {

    private static final String GITHUB_ACCEPT = "application/vnd.github+json";
    private static final ParameterizedTypeReference<List<GithubRepositoryDto>> REPOSITORY_LIST =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<GithubBranchDto>> BRANCH_LIST =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    GithubClient(RestClient.Builder builder, @Value("${github.api.base-url}") String baseUrl) {
        this.restClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Accept", GITHUB_ACCEPT)
                .build();
    }

    List<GithubRepositoryDto> fetchRepositories(String username) {
        return restClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
                    if (response.getStatusCode().value() == 404) {
                        throw new UserNotFoundException(username);
                    }
                    throw new IllegalStateException(
                            "Unexpected GitHub response: " + response.getStatusCode());
                })
                .body(REPOSITORY_LIST);
    }

    List<GithubBranchDto> fetchBranches(String owner, String repository) {
        return restClient.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repository)
                .retrieve()
                .body(BRANCH_LIST);
    }
}
