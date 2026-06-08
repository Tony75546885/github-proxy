package com.example.githubproxy;

record GithubRepositoryDto(String name, GithubOwnerDto owner, boolean fork) {
}
