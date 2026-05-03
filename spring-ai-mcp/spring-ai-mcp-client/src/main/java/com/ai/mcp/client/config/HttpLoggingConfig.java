package com.ai.mcp.client.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * 查看 springai 框架发给大模型的请求和响应
 */
@Configuration
public class HttpLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger("springai.http");

    @Bean
    public RestClientCustomizer restClientLoggingCustomizer() {
        return builder -> builder.requestInterceptor(new LoggingInterceptor());
    }

    private static class LoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
            // 请求行
            log.info("\n>>> {} {}\n>>> Headers: {}", request.getMethod(),
                    UriComponentsBuilder.fromUri(request.getURI()).replaceQuery(null).toUriString(),
                    sanitizeHeaders(request));

            // 请求体
            if (body.length > 0) {
                log.info(">>> Body:\n{}", new String(body, StandardCharsets.UTF_8));
            }

            long start = System.currentTimeMillis();
            ClientHttpResponse response = execution.execute(request, body);
            long duration = System.currentTimeMillis() - start;

            // 响应状态
            log.info("<<< {} ({}ms)", response.getStatusCode(), duration);

            // 响应体（只能读一次，这里读完用缓存包装回去）
            String responseBody = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            log.info("<<< Body:\n{}", truncate(responseBody, 2000));

            // 包装回可重复读取的响应
            return new CachedBodyHttpResponse(response, responseBody);
        }

        private String sanitizeHeaders(HttpRequest request) {
            var headers = request.getHeaders();
            var sanitized = new java.util.LinkedHashMap<String, String>();
            headers.forEach((key, values) -> {
                if (key.equalsIgnoreCase("authorization") || key.equalsIgnoreCase("api-key")) {
                    sanitized.put(key, "***");
                } else {
                    sanitized.put(key, String.join(", ", values));
                }
            });
            return sanitized.toString();
        }

        private String truncate(String s, int max) {
            return s.length() <= max ? s : s.substring(0, max) + "\n... (truncated, " + s.length() + " chars)";
        }
    }

    /**
     * 将响应体缓存起来，使 getBody() 可重复读取。
     */
    private static class CachedBodyHttpResponse implements ClientHttpResponse {

        private final ClientHttpResponse original;
        private final byte[] cachedBody;

        CachedBodyHttpResponse(ClientHttpResponse original, String body) {
            this.original = original;
            this.cachedBody = body.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
            return original.getStatusCode();
        }

        @Override
        public String getStatusText() throws IOException {
            return original.getStatusText();
        }

        @Override
        public org.springframework.http.HttpHeaders getHeaders() {
            return original.getHeaders();
        }

        @Override
        public java.io.InputStream getBody() {
            return new java.io.ByteArrayInputStream(cachedBody);
        }

        @Override
        public void close() {
            original.close();
        }
    }
}
