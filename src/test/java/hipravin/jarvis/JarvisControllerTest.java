package hipravin.jarvis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.SearchProviderType;
import hipravin.jarvis.exception.NotFoundException;
import hipravin.jarvis.github.GithubApiClient;
import hipravin.jarvis.github.jackson.model.CodeSearchResult;
import hipravin.jarvis.googlebooks.GoogleBooksApiClient;
import hipravin.jarvis.googlebooks.jackson.model.BooksVolumes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = {"test"})
class JarvisControllerTest {
    @LocalServerPort
    int port;
    @MockitoBean
    GithubApiClient githubApiClient;

    @MockitoBean
    GoogleBooksApiClient googleBooksApiClient;

    @MockitoBean
    BookstoreDao bookstoreDao;

    private CsrfAwareHttpClient testHttpClient;

    private static final String TEST_SEARCH = "test search";
    private static final String ERROR_SEARCH = "error search";
    private static final String BLANK_SEARCH = " ";

    @BeforeEach
    void setUp() throws IOException, InterruptedException {
        given(this.githubApiClient.searchApprovedAuthors(TEST_SEARCH)).willReturn(emptySearchResult());
        given(this.githubApiClient.searchApprovedAuthors(BLANK_SEARCH)).willReturn(emptySearchResult());
        given(this.githubApiClient.searchApprovedAuthors(ERROR_SEARCH)).willThrow(new RuntimeException("expected error gh"));

        given(this.googleBooksApiClient.search(TEST_SEARCH)).willReturn(emptyVolumes());
        given(this.googleBooksApiClient.search(BLANK_SEARCH)).willReturn(emptyVolumes());
        given(this.googleBooksApiClient.search(ERROR_SEARCH)).willThrow(new RuntimeException("expected error gb"));

        doThrow(new NotFoundException("Book 1234567")).when(this.bookstoreDao).writePdfContentTo(eq(1234567L), any());

        testHttpClient = new CsrfAwareHttpClient("http://localhost:%d".formatted(port));
        var welcomeResponse = testHttpClient.get("/");
        assertEquals(HttpStatus.OK.value(), welcomeResponse.statusCode());
    }

    @Test
    void testSimpleSearch() throws IOException, InterruptedException {
        var response = search(TEST_SEARCH);
        assertEquals(HttpStatus.OK.value(), response.statusCode());
    }

    @Test
    void testBlankSearch() throws IOException, InterruptedException {
        var response = search(BLANK_SEARCH);
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
    }

    @Test
    void testUnexpectedException() throws IOException, InterruptedException {
        var errorResponse = search(ERROR_SEARCH);
        assertTrue(errorResponse.body().contains("expected error gh"));
        assertTrue(errorResponse.body().contains("expected error gb"));
    }

    @Test
    void testBookNotFound() throws IOException, InterruptedException {
        var errorResponse = testHttpClient.get("/api/v1/bookstore/book/1234567/rawpdf#page=0");
        assertEquals(404, errorResponse.statusCode());
        assertTrue(errorResponse.body().contains("1234567"));
    }

    HttpResponse<String> search(String query) throws IOException, InterruptedException {
        return testHttpClient.post("/api/v1/jarvis/query", searchRequestBody(query));
    }

    String searchRequestBody(String query) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(new JarvisRequest(query, EnumSet.of(SearchProviderType.GITHUB, SearchProviderType.GOOGLE_BOOKS)));
    }

    static CodeSearchResult emptySearchResult() {
        return new CodeSearchResult(0, false, Collections.emptyList());
    }

    static BooksVolumes emptyVolumes() {
        return new BooksVolumes("books#volumes", 0L, Collections.emptyList());
    }
}