package hipravin.jarvis.stackexchange.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import hipravin.jarvis.TestUtils;
import hipravin.jarvis.stackexchange.client.dto.SearchExcerpt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "stackexchange.api-base-url=${wiremock.server.baseUrl}")
@EnableWireMock
@EmbeddedKafka
@ActiveProfiles("test")
class StackExchangeApiClientImplTest {
    String sampleResponseResilience4j = TestUtils.loadFromClasspath("data/stackexchange/sample-resilience4j.json");

    @InjectWireMock
    WireMockServer wireMock;

    @Autowired
    StackExchangeApiClient stackExchangeApiClient;

    @BeforeEach
    void setUp() {
        wireMock.resetRequests();
    }

    @Test
    void retryFor500() {
        wireMock.stubFor(get(urlPathEqualTo("/search/excerpts"))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(RuntimeException.class, () -> {
            stackExchangeApiClient.searchExcerpts("any");
        });

        //maxAttempts: 3
        verify(exactly(3), getRequestedFor(urlPathEqualTo("/search/excerpts")));
    }

    @Test
    void sample200() {
        wireMock.stubFor(get(urlPathEqualTo("/search/excerpts"))
                .willReturn(aResponse()
                        .withBody(sampleResponseResilience4j)
                        .withHeader("content", "application/json")
                        .withStatus(200)));

        var excerpts = stackExchangeApiClient.searchExcerpts("resilience4j circuitbreaker");
        assertNotNull(excerpts);
        assertEquals(30, excerpts.items().size());
        SearchExcerpt excerpt = excerpts.items().getFirst();
        assertEquals(56132097L, excerpt.questionId());
    }
}