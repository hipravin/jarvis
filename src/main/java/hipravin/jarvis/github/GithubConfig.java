package hipravin.jarvis.github;

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
@EnableConfigurationProperties(value = {GithubProperties.class})
public class GithubConfig {

    private final GithubProperties githubProperties;

    public GithubConfig(GithubProperties githubProperties) {
        this.githubProperties = githubProperties;
    }

    @Bean
    @Qualifier("githubHttpClientBuilder")
    public HttpClient.Builder githubHttpClientBuilder() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(githubProperties.connectionTimeoutMillis()));
    }

    @Bean
    @Qualifier("githubHttpRequestBuilder")
    public HttpRequest.Builder githubHttpRequestBuilder() {
        return HttpRequest.newBuilder()
                .header(HttpHeaders.ACCEPT, "application/vnd.github.text-match+json")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubProperties.token());
    }


}
