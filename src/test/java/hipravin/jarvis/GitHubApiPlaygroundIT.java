package hipravin.jarvis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHContentSearchBuilder;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@SpringBootTest
@Disabled //itlocal tests are intended for manual execution only
@ActiveProfiles(profiles = {"itlocal"})
public class GitHubApiPlaygroundIT {
    @Value("${github.token}")
    private String githubToken;

    //The "Search code" endpoint requires you to authenticate and limits you to 10 requests per minute

    /*
    Limitations on query length
    You cannot use queries that:

    are longer than 256 characters (not including operators or qualifiers).
    have more than five AND, OR, or NOT operators.
     */
    @Test
    void testQuery() throws IOException {

        GitHub github = GitHub.connect("hipravin", githubToken);
        GHContentSearchBuilder sampleSearchBuilder = github.searchContent().user("hipravin").q("immutable");
        PagedSearchIterable<GHContent> result = sampleSearchBuilder.list();

        for (GHContent ghContent : result) {
            System.out.println(ghContent.getName());
        }
    }

    @Test
    void testHttpClient() {
        try {
            var httpClient = HttpClient.newBuilder()
                    .build();
            var request = HttpRequest.newBuilder().uri(URI.create("https://api.github.com/search/code?q=user:hipravin+immutable"))
                    .header(HttpHeaders.ACCEPT, "application/vnd.github.text-match+json")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Link: " + response.headers().firstValue("Link").orElse("No 'Link' header found"));
            if(response.statusCode() != 200) {
                throw new RuntimeException("Request failed: %s".formatted(response.statusCode()));
            }

            System.out.println(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
