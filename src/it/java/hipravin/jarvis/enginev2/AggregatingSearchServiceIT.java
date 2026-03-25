package hipravin.jarvis.enginev2;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.dto.Excerpt;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static hipravin.jarvis.engine.model.InformationSource.*;
import static org.junit.jupiter.api.Assertions.*;

@JarvisIntegrationTest
class AggregatingSearchServiceIT {
    private static final Logger log = LoggerFactory.getLogger(AggregatingSearchServiceIT.class);

    @Autowired
    AggregatingSearchService searchService;

    @Test
    void searchSample() {
        SearchResponse response = searchService.search(new SearchRequest(
                "resilience4j circuitbreaker", EnumSet.allOf(InformationSource.class)));

        assertNotNull(response);
        assertTrue(response.errors().isEmpty());
        assertTrue(response.excerpts().size() > 10);

        var sources = response.excerpts().stream()
                .map(Excerpt::source)
                .collect(Collectors.toSet());

        assertTrue(sources.contains(GITHUB));
        assertTrue(sources.contains(STACKEXCHANGE));
        assertTrue(sources.contains(GOOGLE_BOOKS));
        //expect nothing from bookstore since it's not populated
        assertFalse(sources.contains(BOOKSTORE));

        shallowCheckFirstOfKind(response, GITHUB);
        shallowCheckFirstOfKind(response, STACKEXCHANGE);
        shallowCheckFirstOfKind(response, GOOGLE_BOOKS);
    }

    void shallowCheckFirstOfKind(SearchResponse response, InformationSource source) {
        var excerptOpt = response.excerpts().stream()
                .filter(e -> e.source() == source)
                .findFirst();

        assertTrue(excerptOpt.isPresent());
        log.info(excerptOpt.get().toString());
    }
}