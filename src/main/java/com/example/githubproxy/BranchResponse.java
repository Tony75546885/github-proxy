package com.example.githubproxy;

record BranchResponse(String name, String lastCommitSha) {

    static BranchResponse from(Branch branch) {
        return new BranchResponse(branch.name(), branch.lastCommitSha());
    }
}
