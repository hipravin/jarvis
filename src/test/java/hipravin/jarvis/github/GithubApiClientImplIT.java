package hipravin.jarvis.github;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
class GithubApiClientImplIT {

    @Autowired
    GithubApiClient githubApiClient;

    @Test
    void testSampleSearch() {

//        var result = githubApiClient.search("user:hipravin immutable");
        var result = githubApiClient.searchApprovedAuthors("int");
        Set<String> authors = result.codeSearchItems().stream()
                .map(item -> item.repository().owner().login())
                .collect(Collectors.toSet());

        System.out.println(authors);

        System.out.printf("incomplete results: %b, total: %d", result.incompleteResults(), result.count());
    }
}