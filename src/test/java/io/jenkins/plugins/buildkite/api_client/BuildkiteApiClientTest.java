package io.jenkins.plugins.buildkite.api_client;

import hudson.util.Secret;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BuildkiteApiClientTest {

    private BuildkiteApiClient client;
    private Secret mockSecret;

    @Mock private CloseableHttpClient mockHttpClient;
    @Mock private CloseableHttpResponse mockResponse;
    @Mock private HttpEntity mockEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockSecret = Secret.fromString("test-api-token");
    }

    @Test
    void constructor_setsApiToken() {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::getCloseableHttpClient).thenReturn(mockHttpClient);
            client = new BuildkiteApiClient(mockSecret);

            assertNotNull(client);
            mockedHttpClient.verify(HttpClient::getCloseableHttpClient);
        }
    }

    @Test
    void createBuild_success_returnsBuildkiteBuild() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            String responseJson = """
                    {
                      "id": "2d841f58-6dd6-44f9-94ff-06d4ec7a6a0f",
                      "number": 42,
                      "state": "running",
                      "web_url": "https://buildkite.com/my-org/my-pipeline/builds/42",
                      "commit": "abc123def",
                      "branch": "main",
                      "url": "https://api.buildkite.com/my-org/my-pipeline/builds/42"
                    }
                    """;

            client = mockClientReturningHttpResponse(mockedHttpClient, 201, responseJson);

            var request = new CreateBuildRequest()
                    .setCommit("abc123def")
                    .setBranch("main")
                    .setMessage("Test build");

            BuildkiteBuild result = client.createBuild("my-org", "my-pipeline", request);

            assertNotNull(result);
            assertEquals("2d841f58-6dd6-44f9-94ff-06d4ec7a6a0f", result.getId());
            assertEquals(42, result.getNumber());
            assertEquals("running", result.getState());
            assertEquals("https://buildkite.com/my-org/my-pipeline/builds/42", result.getWebUrl());
            assertEquals("abc123def", result.getCommit());
            assertEquals("main", result.getBranch());
            assertEquals("https://api.buildkite.com/my-org/my-pipeline/builds/42", result.getUrl());

            verify(mockHttpClient).execute(any(HttpPost.class));
        }
    }

    @Test
    void createBuild_apiError_throwsBuildkiteApiException() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            String errorResponseBody = "Pipeline not found";
            client = mockClientReturningHttpResponse(mockedHttpClient, 404, errorResponseBody);

            var request = new CreateBuildRequest()
                    .setCommit("abc123def")
                    .setBranch("main")
                    .setMessage("Test build");

            var exception = assertThrows(BuildkiteApiException.class, () -> {
                client.createBuild("my-org", "nonexistent-pipeline", request);
            });

            assertEquals(404, exception.getStatusCode());
            assertEquals("Pipeline not found", exception.getResponseBody());
        }
    }

    @Test
    void createBuild_ioException_throwsRuntimeException() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            client = mockClientThrowingIOException(mockedHttpClient, "Network error");

            var request = new CreateBuildRequest()
                    .setCommit("abc123def")
                    .setBranch("main")
                    .setMessage("Test build");

            var exception = assertThrows(RuntimeException.class, () -> {
                client.createBuild("my-org", "my-pipeline", request);
            });

            assertInstanceOf(IOException.class, exception.getCause());
            assertEquals("Network error", exception.getCause().getMessage());
        }
    }

    @Test
    void getBuild_success_returnsBuildkiteBuild() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            String responseJson = """
                    {
                      "id": "46e39f6d-0647-4ecb-9d4d-09f5cf780502",
                      "number": 99,
                      "state": "passed",
                      "web_url": "https://buildkite.com/my-org/my-pipeline/builds/99",
                      "commit": "def456abc",
                      "branch": "feature-branch",
                      "url": "https://api.buildkite.com/my-org/my-pipeline/builds/99"
                    }
                    """;

            client = mockClientReturningHttpResponse(mockedHttpClient, 200, responseJson);

            BuildkiteBuild result = client.getBuild("my-org", "my-pipeline", 99);

            assertNotNull(result);
            assertEquals("46e39f6d-0647-4ecb-9d4d-09f5cf780502", result.getId());
            assertEquals(99, result.getNumber());
            assertEquals("passed", result.getState());
            assertEquals("https://buildkite.com/my-org/my-pipeline/builds/99", result.getWebUrl());
            assertEquals("def456abc", result.getCommit());
            assertEquals("feature-branch", result.getBranch());
            assertEquals("https://api.buildkite.com/my-org/my-pipeline/builds/99", result.getUrl());

            verify(mockHttpClient).execute(any(HttpGet.class));
        }
    }

    @Test
    void getBuild_notFound_throwsBuildkiteApiException() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            client = mockClientReturningHttpResponse(mockedHttpClient, 404, "Build not found");

            var exception = assertThrows(BuildkiteApiException.class, () -> {
                client.getBuild("my-org", "my-pipeline", 999);
            });

            assertEquals(404, exception.getStatusCode());
            assertEquals("Build not found", exception.getResponseBody());
        }
    }

    @Test
    void getBuild_ioException_throwsRuntimeException() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            client = mockClientThrowingIOException(mockedHttpClient, "Connection timeout");

            var exception = assertThrows(RuntimeException.class, () -> {
                client.getBuild("my-org", "my-pipeline", 123);
            });

            assertInstanceOf(IOException.class, exception.getCause());
            assertEquals("Connection timeout", exception.getCause().getMessage());
        }
    }

    @Test
    void handleResponse_serverError_throwsBuildkiteApiException() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            client = mockClientReturningHttpResponse(mockedHttpClient, 500, "Internal server error");

            var request = new CreateBuildRequest()
                    .setCommit("abc123def")
                    .setBranch("main")
                    .setMessage("Test build");

            var exception = assertThrows(BuildkiteApiException.class, () -> {
                client.createBuild("my-org", "my-pipeline", request);
            });

            assertEquals(500, exception.getStatusCode());
            assertEquals("Internal server error", exception.getResponseBody());
        }
    }

    @Test
    void handleResponse_nullResponseBody_throwsBuildkiteApiExceptionWithEmptyBody() throws Exception {
        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            client = mockClientReturningHttpResponseWithNullEntity(mockedHttpClient, 400);

            var request = new CreateBuildRequest()
                    .setCommit("abc123def")
                    .setBranch("main")
                    .setMessage("Test build");

            var exception = assertThrows(BuildkiteApiException.class, () -> {
                client.createBuild("my-org", "my-pipeline", request);
            });

            assertEquals(400, exception.getStatusCode());
            assertEquals("", exception.getResponseBody());
        }
    }

    private BuildkiteApiClient mockClientReturningHttpResponse(MockedStatic<HttpClient> mockedHttpClient, int statusCode, String responseBody) throws IOException {
        mockedHttpClient.when(HttpClient::getCloseableHttpClient).thenReturn(mockHttpClient);

        when(mockResponse.getCode()).thenReturn(statusCode);
        when(mockResponse.getEntity()).thenReturn(mockEntity);
        when(mockEntity.getContent()).thenReturn(new ByteArrayInputStream(responseBody.getBytes(StandardCharsets.UTF_8)));
        when(mockHttpClient.execute(any(ClassicHttpRequest.class))).thenReturn(mockResponse);

        return new BuildkiteApiClient(mockSecret);
    }

    private BuildkiteApiClient mockClientThrowingIOException(MockedStatic<HttpClient> mockedHttpClient, String errorMessage) throws IOException {
        mockedHttpClient.when(HttpClient::getCloseableHttpClient).thenReturn(mockHttpClient);

        when(mockHttpClient.execute(any(ClassicHttpRequest.class))).thenThrow(new IOException(errorMessage));

        return new BuildkiteApiClient(mockSecret);
    }

    private BuildkiteApiClient mockClientReturningHttpResponseWithNullEntity(MockedStatic<HttpClient> mockedHttpClient, int statusCode) throws IOException {
        mockedHttpClient.when(HttpClient::getCloseableHttpClient).thenReturn(mockHttpClient);

        when(mockResponse.getCode()).thenReturn(statusCode);
        when(mockResponse.getEntity()).thenReturn(null);
        when(mockHttpClient.execute(any(ClassicHttpRequest.class))).thenReturn(mockResponse);

        return new BuildkiteApiClient(mockSecret);
    }
}
