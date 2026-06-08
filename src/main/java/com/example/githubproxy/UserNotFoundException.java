package com.example.githubproxy;

final class UserNotFoundException extends RuntimeException {

    UserNotFoundException(String username) {
        super("GitHub user '" + username + "' was not found");
    }
}
