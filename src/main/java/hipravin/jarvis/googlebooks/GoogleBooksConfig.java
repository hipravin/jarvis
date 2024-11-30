package hipravin.jarvis.googlebooks;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(value = {GoogleBooksProperties.class})
public class GoogleBooksConfig {

    @Value("${googlebooks.connection.timeout}")
    private long googlebooksConnectionTimeoutMillis;

    @Bean
    @Qualifier("googlebooksHttpClientBuilder")
    public HttpClient.Builder googlebooksHttpClientBuilder() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(googlebooksConnectionTimeoutMillis));
    }

    @Bean
    @Qualifier("googlebooksHttpRequestBuilder")
    public HttpRequest.Builder googlebooksHttpRequestBuilder() {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.ACCEPT, "application/json");
    }
}
