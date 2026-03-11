package hipravin.jarvis.stackexchange.client;

import hipravin.jarvis.JarvisIntegrationTest;
import hipravin.jarvis.stackexchange.client.dto.ResponseItems;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@JarvisIntegrationTest
class StackExchangeApiClientImplIT {
    @Autowired
    StackExchangeApiClient apiClient;

    @Test
    void sampleSearchExcerpts() {
        ResponseItems<SearchExcerpt> response = apiClient.searchExcerpts("resilience4j circuitbreaker");

        assertNotNull(response);
        assertEquals(30, response.items().size());
        assertTrue(response.hasMore());

        var excerpt = response.items().getFirst();
        assertTrue(excerpt.body().toLowerCase().contains("resilience"));
        assertTrue(excerpt.excerpt().toLowerCase().contains("resilience"));
    }
}