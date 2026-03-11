package hipravin.jarvis.stackexchange.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(StackExchangeProperties.class)
public class Config {
    private final StackExchangeProperties stackExchangeProperties;

    public Config(StackExchangeProperties stackExchangeProperties) {
        this.stackExchangeProperties = stackExchangeProperties;
    }

    @Bean
    @Qualifier("stackExchangeHttpClient")
    public HttpClient stackExchangeHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(stackExchangeProperties.connectionTimeoutMillis()))
                .build();
    }

    @Bean
    @Qualifier("stackExchangeHttpRequestBuilder")
    public HttpRequest.Builder stackExchangeHttpRequestBuilder() {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.ACCEPT, "application/json");
    }

}
