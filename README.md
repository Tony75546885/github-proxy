# github-proxy

A small Spring Boot proxy that exposes a single endpoint listing a GitHub user's
non-fork repositories together with their branches.

## Tech stack

- Java 25
- Spring Boot 4 (Spring MVC, no WebFlux)
- Gradle (Kotlin DSL)
- WireMock 3 (integration tests only)

## API

### List non-fork repositories of a user

```
GET /users/{username}/repositories
```

`200 OK` — JSON array, one entry per non-fork repository:

```json
[
  {
    "repositoryName": "hello-world",
    "ownerLogin": "octocat",
    "branches": [
      { "name": "main", "lastCommitSha": "abc123" },
      { "name": "dev",  "lastCommitSha": "def456" }
    ]
  }
]
```

`404 Not Found` — when the GitHub user does not exist:

```json
{
  "status": 404,
  "message": "GitHub user 'ghost' was not found"
}
```

## Configuration

| Property              | Default                  | Description                          |
|-----------------------|--------------------------|--------------------------------------|
| `github.api.base-url` | `https://api.github.com` | Base URL of the backing GitHub API.  |

The application calls the public GitHub REST API
(<https://developer.github.com/v3>) without authentication. Anonymous calls are
rate-limited to 60 requests per hour per IP; override `github.api.base-url` to
point at a different host (for example, an enterprise instance).

## Build & run

The project is set up with the Gradle Kotlin DSL but does not include the
Gradle wrapper binary. Generate it once with a local Gradle 8.10+ install:

```bash
gradle wrapper
```

Then build and run:

```bash
./gradlew build
./gradlew bootRun
```

The application starts on port `8080` by default.

## Tests

Only integration tests are provided. They boot the full application context on a
random port and use WireMock to emulate GitHub.

```bash
./gradlew test
```

Two scenarios are covered, one per acceptance criterion:

1. An existing user returns only non-fork repositories, each with its branches
   and last commit SHA. Forked repositories are skipped and their branches are
   never fetched.
2. A non-existing user produces a `404` response with the `{ status, message }`
   payload.

## Notes

- Pagination is not handled, neither on the exposed endpoint nor on calls to
  GitHub. Only the first page returned by GitHub is consumed.
- No authentication / security layer is configured.
- Two model categories are kept: internal business records (`Repository`,
  `Branch`) and DTOs at the HTTP boundary (`Github*Dto` inbound,
  `*Response` outbound). The architecture is a flat
  Controller / Service / Client trio inside a single package.
