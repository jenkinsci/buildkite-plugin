package io.jenkins.plugins.buildkite.api_client;

import hudson.ProxyConfiguration;
import hudson.Util;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultAuthenticationStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.core5.http.HttpHost;
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
        Jenkins jenkinsInstance = Jenkins.getInstanceOrNull();
        ProxyConfiguration proxy = jenkinsInstance != null ? jenkinsInstance.proxy : null;

        if (proxy != null && Util.fixEmpty(proxy.name) != null) {
            var proxyHost = new HttpHost(proxy.name, proxy.port);
            var routePlanner = new DefaultProxyRoutePlanner(proxyHost);

            clientBuilder.setRoutePlanner(routePlanner);

            String proxyUser = Util.fixEmpty(proxy.getUserName());
            char[] proxyPassword = Secret.toString(proxy.getSecretPassword()).toCharArray();

            if (proxyUser != null) {
                var authScope = new AuthScope(proxy.name, proxy.port);
                var credentials = new UsernamePasswordCredentials(proxyUser, proxyPassword);
                credentialsProvider.setCredentials(authScope, credentials);
                clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                clientBuilder.setProxyAuthenticationStrategy(new DefaultAuthenticationStrategy());
            }
        }

        clientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        return clientBuilder;
    }

    public static CloseableHttpClient getCloseableHttpClient() {
        return getCloseableHttpClientBuilder().build();
    }
}
