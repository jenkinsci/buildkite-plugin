package io.jenkins.plugins.buildkite.api_client;

public class BuildkiteApiException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

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

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
