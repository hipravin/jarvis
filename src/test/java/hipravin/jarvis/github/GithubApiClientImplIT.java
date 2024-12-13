package hipravin.jarvis.github;

import hipravin.jarvis.github.jackson.model.CodeSearchItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles(profiles = {"itlocal"})
class GithubApiClientImplIT {

    @Autowired
    GithubApiClient githubApiClient;

    @Test
    void testSampleSearch() {

//        var result = githubApiClient.searchApprovedAuthors("translate.google.com");
//        var result = githubApiClient.searchApprovedAuthors("filename:Dockerfile from addgroup JarLauncher");
        var result = githubApiClient.searchApprovedAuthors("globalexceptionhandler");
        Set<String> authors = result.codeSearchItems().stream()
                .map(item -> item.repository().owner().login())
                .collect(Collectors.toSet());

        System.out.println("total authors: " + authors.size() + ", " + authors);

        System.out.printf("incomplete results: %b, total: %d%n", result.incompleteResults(), result.count());

        long distinctUrls = result.codeSearchItems().stream()
                .map(item -> item.url())
                .distinct()
                .count();

        System.out.println("Distinct urls: " + distinctUrls);

        for (CodeSearchItem codeSearchItem : result.codeSearchItems()) {
            System.out.println(codeSearchItem.repository().owner().login() + ": " + codeSearchItem.name() + " " + codeSearchItem.htmlUrl());
        }
        if (result.count() > 0) {
            var csi = result.codeSearchItems().get(0);
            var content = githubApiClient.getContent(csi.url());
            System.out.printf("%n====%s======%n%s%n==========%n", csi.url(), content);
        }

    }
}