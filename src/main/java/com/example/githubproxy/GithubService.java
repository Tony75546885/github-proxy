package com.example.githubproxy;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
final class GithubService {

    private final GithubClient client;

    GithubService(GithubClient client) {
        this.client = client;
    }

    List<Repository> listNonForkRepositories(String username) {
        return client.fetchRepositories(username).stream()
                .filter(repo -> !repo.fork())
                .map(this::toRepository)
                .toList();
    }

    private Repository toRepository(GithubRepositoryDto repo) {
        String ownerLogin = repo.owner().login();
        List<Branch> branches = client.fetchBranches(ownerLogin, repo.name()).stream()
                .map(branch -> new Branch(branch.name(), branch.commit().sha()))
                .toList();
        return new Repository(repo.name(), ownerLogin, branches);
    }
}
