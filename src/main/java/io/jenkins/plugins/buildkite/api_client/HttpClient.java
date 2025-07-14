package io.jenkins.plugins.buildkite.api_client;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;

class HttpClient {
    public static HttpClientBuilder getCloseableHttpClientBuilder() {
        int timeoutInSeconds = 60;

        var connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(timeoutInSeconds))
                .setSocketTimeout(Timeout.ofSeconds(timeoutInSeconds)).build();

        var connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(connectionConfig)
                .build();

        var requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(timeoutInSeconds)).build();

        HttpClientBuilder clientBuilder = HttpClients
                .custom()
                .useSystemProperties()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig);

        var credentialsProvider = new BasicCredentialsProvider();
        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);

        return clientBuilder;
    }

    public static CloseableHttpClient getCloseableHttpClient() {
        return getCloseableHttpClientBuilder().build();
    }
}
