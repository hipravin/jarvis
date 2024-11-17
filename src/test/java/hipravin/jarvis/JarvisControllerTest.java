package hipravin.jarvis;

import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
class JarvisControllerTest {
    @LocalServerPort
    int port;

    @MockBean
    GithubApiClient githubApiClient;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String TEST_SEARCH = "test search";
    private static final String ERROR_SEARCH = "error search";
    private static final String BLANK_SEARCH = " ";

    @BeforeEach
    void setUp() {
        given(this.githubApiClient.searchApprovedAuthors(TEST_SEARCH)).willReturn(emptySearchResult());
        given(this.githubApiClient.searchApprovedAuthors(BLANK_SEARCH)).willReturn(emptySearchResult());
        given(this.githubApiClient.searchApprovedAuthors(ERROR_SEARCH)).willThrow(new RuntimeException("expected error"));
    }

    <T> ResponseEntity<T> searchCode(String query, Class<T> clazz) {
        return restTemplate.postForEntity(URI.create("http://localhost:%d/api/v1/jarvis/query".formatted(port)),
                Map.of("query", query), clazz);
    }

    @Test
    void testSimpleSearch() {
        ResponseEntity<CodeSearchResult> csre = searchCode("test search", CodeSearchResult.class);
        assertEquals(HttpStatus.OK, csre.getStatusCode());

        System.out.println(csre.getBody());
    }

    @Test
    void testBlankSearch() {
        ResponseEntity<ProblemDetail> errorResponse = searchCode(" ", ProblemDetail.class);
        assertEquals(HttpStatus.BAD_REQUEST, errorResponse.getStatusCode());
        System.out.println(errorResponse.getBody());
    }

    @Test
    void testUnexpectedException() {
        ResponseEntity<ProblemDetail> errorResponse = searchCode("error search", ProblemDetail.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorResponse.getStatusCode());

        System.out.println(errorResponse.getBody());
    }

    static CodeSearchResult emptySearchResult() {
        return new CodeSearchResult(0, false, Collections.EMPTY_LIST);
    }
}