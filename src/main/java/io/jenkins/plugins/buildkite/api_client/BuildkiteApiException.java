package io.jenkins.plugins.buildkite.api_client;

import lombok.Getter;

public class BuildkiteApiException extends RuntimeException {
    @Getter private final int statusCode;
    @Getter private final String responseBody;

    public BuildkiteApiException(int statusCode, String responseBody) {
        super(String.format("Buildkite API request failed with status %d: %s", statusCode, responseBody));
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public BuildkiteApiException(int statusCode, String responseBody, Throwable cause) {
        super(String.format("Buildkite API request failed with status %d: %s", statusCode, responseBody), cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
