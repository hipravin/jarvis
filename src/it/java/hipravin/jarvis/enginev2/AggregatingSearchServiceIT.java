package hipravin.jarvis.enginev2;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.dto.Excerpt;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import hipravin.jarvis.statistic.StatisticConsumer;
import hipravin.jarvis.statistic.StatisticGathererService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.EnumSet;
import java.util.stream.Collectors;

import static hipravin.jarvis.engine.model.InformationSource.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@JarvisIntegrationTest
class AggregatingSearchServiceIT {
    private static final Logger log = LoggerFactory.getLogger(AggregatingSearchServiceIT.class);

    @Autowired
    AggregatingSearchService searchService;

    @Autowired
    @MockitoSpyBean
    StatisticGathererService statisticService;

    @Autowired
    @MockitoSpyBean
    StatisticConsumer statisticConsumer;

    @Test
    void searchSample() throws Exception {
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

        Thread.sleep(1500);
        verify(statisticService, atLeastOnce()).onSearchCompleted(any());
        verify(statisticConsumer, atLeastOnce()).searchStatListener(any(), any(), anyInt(), anyLong());
        Thread.sleep(20000);
    }

    void shallowCheckFirstOfKind(SearchResponse response, InformationSource source) {
        var excerptOpt = response.excerpts().stream()
                .filter(e -> e.source() == source)
                .findFirst();

        assertTrue(excerptOpt.isPresent());
        log.info(excerptOpt.get().toString());
    }
}