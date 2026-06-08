package com.example.githubproxy;

import java.util.List;

record RepositoryResponse(String repositoryName, String ownerLogin, List<BranchResponse> branches) {

    static RepositoryResponse from(Repository repository) {
        List<BranchResponse> branches = repository.branches().stream()
                .map(BranchResponse::from)
                .toList();
        return new RepositoryResponse(repository.name(), repository.ownerLogin(), branches);
    }
}
