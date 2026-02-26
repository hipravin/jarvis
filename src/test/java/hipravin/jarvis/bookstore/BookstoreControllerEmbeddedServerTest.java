package hipravin.jarvis.bookstore;

import hipravin.jarvis.bookstore.dao.BookstoreDao;
import hipravin.jarvis.bookstore.load.BookReader;
import hipravin.jarvis.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static hipravin.jarvis.TestUtils.httpGet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka
@ActiveProfiles(profiles = {"test"})
class BookstoreControllerEmbeddedServerTest {
    @LocalServerPort
    int port;

    @MockitoBean
    BookstoreDao bookstoreDao;
    @MockitoBean
    BookReader bookReader;

    @Test
    void rawpdfNotFound() throws Exception {
        doThrow(new NotFoundException("Book '1404'"))
                .when(bookstoreDao).writePdfContentTo(eq(1404L), any());

        var response = httpGet(port, "/api/v1/bookstore/book/1404/rawpdf");
        assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
        assertTrue(response.body().contains("1404"));
        assertTrue(response.headers().firstValue("content-type").stream()
                .anyMatch(s -> s.equals(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void rawpdfBadRequest() throws Exception {
        var response = httpGet(port, "/api/v1/bookstore/book/123412341234123412341234123412341234/rawpdf");
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
        assertTrue(response.body().contains("Bad"));
        assertTrue(response.headers().firstValue("content-type").stream()
                .anyMatch(s -> s.equals(MediaType.APPLICATION_JSON_VALUE)));
    }

    @Test
    void rawPdfUnexpected() throws Exception {
        var exceptionMessage = "<Unexpected error detail>";

        doThrow(new RuntimeException(exceptionMessage))
                .when(bookstoreDao).writePdfContentTo(eq(500L), any());

        var response = httpGet(port, "/api/v1/bookstore/book/500/rawpdf");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.statusCode());

        assertFalse(response.body().contains(exceptionMessage), "Should not expose error details");
        assertTrue(response.headers().firstValue("content-type").stream()
                .anyMatch(s -> s.equals(MediaType.APPLICATION_JSON_VALUE)));
    }
}