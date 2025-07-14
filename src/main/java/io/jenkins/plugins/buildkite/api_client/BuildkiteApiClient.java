package io.jenkins.plugins.buildkite.api_client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hudson.util.Secret;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;

public class BuildkiteApiClient {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String BUILDKITE_API_BASE = "https://api.buildkite.com/v2";
    private Secret apiToken;
    private CloseableHttpClient httpClient;

    public BuildkiteApiClient(Secret apiToken) {
        this.apiToken = apiToken;
        this.httpClient = HttpClient.getCloseableHttpClient();
    }

    public BuildkiteBuild createBuild(String organization, String pipeline, CreateBuildRequest createBuildRequest) {
        var url = String.format(
                "%s/organizations/%s/pipelines/%s/builds",
                BUILDKITE_API_BASE,
                organization,
                pipeline
        );

        var request = new HttpPost(url);
        request.setHeader("Authorization", String.format("Bearer %s", this.apiToken.getPlainText()));
        request.setHeader("Content-Type", "application/json");

        String requestJson = null;
        try {
            requestJson = MAPPER.writeValueAsString(createBuildRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        request.setEntity(new StringEntity(requestJson));

        CloseableHttpResponse response = null;
        try {
            response = this.httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return responseToBuildkiteBuild(response);
    }

    public BuildkiteBuild getBuild(String organization, String pipeline, int buildNumber) {
        var url = String.format(
                "%s/organizations/%s/pipelines/%s/builds/%s",
                BUILDKITE_API_BASE,
                organization,
                pipeline,
                buildNumber
        );

        var request = new HttpGet(url);
        request.setHeader("Authorization", String.format("Bearer %s", this.apiToken.getPlainText()));
        request.setHeader("Content-Type", "application/json");

        CloseableHttpResponse response = null;
        try {
            response = this.httpClient.execute(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return responseToBuildkiteBuild(response);
    }

    private BuildkiteBuild responseToBuildkiteBuild(CloseableHttpResponse response) {
        var build = new BuildkiteBuild();

        try {
            JsonNode json = MAPPER.readTree(response.getEntity().getContent());

            build
                    .setId(json.get("id").asText())
                    .setNumber(json.get("number").asInt())
                    .setState(json.get("state").asText())
                    .setWebUrl(json.get("web_url").asText())
                    .setCommit(json.get("commit").asText())
                    .setBranch(json.get("branch").asText())
                    .setUrl(json.get("url").asText());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return build;
    }
}
