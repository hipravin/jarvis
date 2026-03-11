package hipravin.jarvis.stackexchange.client;

import hipravin.jarvis.TestUtils;
import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackExchangeJsonMapperImplTest {

    String sampleResponseResilience4j = TestUtils.loadFromClasspath("data/stackexchange/sample-resilience4j.json");
    StackExchangeMapper mapper = new StackExchangeJsonMapperImpl();

    @Test
    void sampleResilience4jRead() {
        ResponseItems<SearchExcerpt> response = mapper.readSearchExcerpts(sampleResponseResilience4j);

        assertNotNull(response);
        assertEquals(30, response.items().size());
        assertTrue(response.hasMore());
        assertEquals(255, response.quotaRemaining());
        assertEquals(300, response.quotaMax());

        SearchExcerpt excerpt = response.items().getFirst();
        assertEquals(56132097L, excerpt.questionId());
        assertEquals("Wrapping Resilience4j circuitbreaker around a service method with multiple arguments", excerpt.title());

        assertTrue(excerpt.body().startsWith("Resilience4j-circuitbreaker allows"));
        assertTrue(excerpt.excerpt().startsWith("<span class=\"highlight\">Resilience4j</span>"));
        //      "last_activity_date": 1559308315,
        //      "creation_date": 1557841887,
        assertEquals(1557841887L, excerpt.creationDate().getEpochSecond());
        assertEquals(1559308315L, excerpt.lastActivityDate().getEpochSecond());

    }
}