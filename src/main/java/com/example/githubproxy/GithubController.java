package com.example.githubproxy;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
final class GithubController {

    private final GithubService service;

    GithubController(GithubService service) {
        this.service = service;
    }

    @GetMapping(value = "/users/{username}/repositories", produces = MediaType.APPLICATION_JSON_VALUE)
    List<RepositoryResponse> listRepositories(@PathVariable String username) {
        return service.listNonForkRepositories(username).stream()
                .map(RepositoryResponse::from)
                .toList();
    }
}
