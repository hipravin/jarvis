package hipravin.jarvis.enginev2;

import hipravin.jarvis.bookstore.BookstoreSearchService;
import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.dto.*;
import hipravin.jarvis.github.GithubSearchService;
import hipravin.jarvis.googlebooks.GoogleBooksSearchService;
import hipravin.jarvis.stackexchange.StackExchangeSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        AggregatingSearchService.class
}, properties = {"jarvis.search-service.timeout-ms=2000"})
class AggregatingSearchServiceTest {

    @MockitoBean
    @Order(SearchService.Order.ORDER_1)
    GithubSearchService githubSearchService;
    @MockitoBean
    @Order(SearchService.Order.ORDER_2)
    BookstoreSearchService bookstoreSearchService;
    @MockitoBean
    @Order(SearchService.Order.ORDER_3)
    StackExchangeSearchService stackExchangeSearchService;
    @MockitoBean
    @Order(SearchService.Order.ORDER_4)
    GoogleBooksSearchService googleBooksSearchService;

    @Autowired
    @Qualifier("aggregatingSearchService")
    SearchService aggregatingService;

    SearchService[] serviceMocks;

    @BeforeEach
    void setUp() {
        serviceMocks = new SearchService[]{
                googleBooksSearchService, stackExchangeSearchService,
                bookstoreSearchService, githubSearchService
        };
        Mockito.reset(serviceMocks);

        for (SearchService serviceMock : serviceMocks) {
            when(serviceMock.search(any())).thenReturn(new SearchResponse(List.of(), List.of()));
            when(serviceMock.getSource()).thenCallRealMethod();
        }
    }

    @Test
    void serviceCallEach() {
        aggregatingService.search(new SearchRequest("java",
                EnumSet.allOf(InformationSource.class)));

        verify(githubSearchService, times(1)).search(any());
        verify(bookstoreSearchService, times(1)).search(any());
        verify(stackExchangeSearchService, times(1)).search(any());
        verify(googleBooksSearchService, times(1)).search(any());
    }

    @Test
    void successfullResponses() {
        when(githubSearchService.search(any())).thenReturn(mockSearchResponse("key1", InformationSource.GITHUB));
        when(bookstoreSearchService.search(any())).thenReturn(mockSearchResponse("key2", InformationSource.BOOKSTORE));
        when(stackExchangeSearchService.search(any())).thenReturn(mockSearchResponse("key3", InformationSource.STACKEXCHANGE));
        when(googleBooksSearchService.search(any())).thenReturn(mockSearchResponse("key4", InformationSource.GOOGLE_BOOKS));

        SearchResponse sr = aggregatingService.search(new SearchRequest("any request",
                EnumSet.allOf(InformationSource.class)));

        assertTrue(sr.errors().isEmpty());
        assertEquals(4, sr.excerpts().size());

        assertEquals("title key1", sr.excerpts().get(0).title().title());
        assertEquals("title key2", sr.excerpts().get(1).title().title());
        assertEquals("title key3", sr.excerpts().get(2).title().title());
        assertEquals("title key4", sr.excerpts().get(3).title().title());
    }

    @Test
    void serviceResponseTimedOut() {
        when(githubSearchService.search(any())).thenAnswer(invocation -> {
            Thread.sleep(50000);
            return new SearchResponse(List.of(), List.of());
        });
        when(bookstoreSearchService.search(any())).thenReturn(mockSearchResponse("key1", InformationSource.BOOKSTORE));

        SearchResponse sr = aggregatingService.search(new SearchRequest("any request",
                EnumSet.allOf(InformationSource.class)));

        assertEquals(1, sr.excerpts().size());
        assertEquals("main text block key1", sr.excerpts().getFirst().main().text());

        assertEquals(1, sr.errors().size());
        assertEquals("GH: request timed out", sr.errors().getFirst().message());
    }

    SearchResponse mockSearchResponse(String key, InformationSource source) {
        var singleExcerpt = Excerpt.builder()
                .source(InformationSource.BOOKSTORE)
                .title("title " + key, "http://href/" + key)
                .mainHtml("main text block " + key)
                .build();

        return new SearchResponse(List.of(singleExcerpt), List.of());
    }
}