package hipravin.jarvis.stackexchange;

import hipravin.jarvis.TestUtils;
import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.dto.*;
import hipravin.jarvis.stackexchange.client.StackExchangeApiClient;
import hipravin.jarvis.stackexchange.client.StackExchangeJsonReaderImpl;
import hipravin.jarvis.stackexchange.client.JsonReader;
import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import hipravin.jarvis.stackexchange.mapper.StackExchangeDtoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {StackExchangeDtoMapper.class, StackExchangeSearchService.class})
class StackExchangeSearchServiceTest {

    @Autowired
    StackExchangeSearchService service;

    @MockitoBean
    StackExchangeApiClient stackExchangeApiClient;

    @BeforeEach
    void setUp() {
        Mockito.reset(stackExchangeApiClient);
    }

    @Test
    void searchOk() {
        when(stackExchangeApiClient.searchExcerpts("resilience4j circuitbreaker"))
                .thenReturn(staticResponse("data/stackexchange/sample-resilience4j.json"));
        SearchResponse response = service.search(new SearchRequest("resilience4j circuitbreaker",
                EnumSet.of(InformationSource.STACKEXCHANGE)));
        assertTrue(response.errors().isEmpty());

        List<Excerpt> excerpts = response.excerpts();
        assertEquals(30, excerpts.size());

        Excerpt excerpt = excerpts.getFirst();

        assertEquals(Icons.STACKEXCHANGE_ICON, excerpt.header().icon());
        assertEquals("Wrapping Resilience4j circuitbreaker around a service method with multiple arguments",
                excerpt.header().header().title());

        assertEquals("https://stackoverflow.com/questions/56132097/", excerpt.header().header().href());

        assertEquals(TextFormat.HTML, excerpt.main().format());
        assertTrue(excerpt.main().text().contains(
                "<span class=\"highlight\">Resilience4j</span>-<span class=\"highlight\">circuitbreaker</span>"));
    }

    @Test
    void searchException() {
        String exceptionMessage = "request timeout";
        String query = "query that can't be processed";

        when(stackExchangeApiClient.searchExcerpts(query))
                .thenThrow(new RuntimeException(exceptionMessage));

        var response = service.search(new SearchRequest(query,
                EnumSet.of(InformationSource.STACKEXCHANGE)));

        assertTrue(response.excerpts().isEmpty());
        assertEquals(1, response.errors().size());

        var error = response.errors().getFirst();
        assertTrue(error.message().contains(exceptionMessage));
        assertTrue(error.message().contains(exceptionMessage));
    }

    static ResponseItems<SearchExcerpt> staticResponse(String classpathResource) {
        String json = TestUtils.loadFromClasspath(classpathResource);
        JsonReader mapper = new StackExchangeJsonReaderImpl();

        assertNotNull(json);
        var response = mapper.readSearchExcerpts(json);
        assertNotNull(response);

        return response;
    }
}