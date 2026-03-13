package hipravin.jarvis.resilienceshowcase;

import hipravin.jarvis.JarvisIntegrationTest;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@JarvisIntegrationTest
class ResilientServiceImplTest {
    static Logger log = LoggerFactory.getLogger(ResilientServiceImplTest.class);

    @Autowired
    ResilientServiceImpl resilientService;
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        resilientService.resetCounters();
    }

    @Test
    @Disabled
    void sequential() {
        int calls = 100;
        Set<String> threadNames = new ConcurrentSkipListSet<>();

        long maxConcurrency = 0;

        for (long i = 0; i < calls; i++) {
            var response = resilientService.callExternalService();
            assertEquals(i, response.callCount());
            threadNames.add(response.threadName());
            if(response.concurrentCount() > maxConcurrency) {
                maxConcurrency = response.concurrentCount();
            }
        }

        assertTrue(resilientService.getExceptionCounter().get() > 0);
        assertEquals(1, threadNames.size());
        assertEquals(1, maxConcurrency);
    }

    @Test
    @Disabled
    void concurrent() {
        int calls = 1000;
        try(var executor =  Executors.newVirtualThreadPerTaskExecutor()) {
            var futures = IntStream.range(0, calls)
                    .mapToObj(i -> CompletableFuture.supplyAsync(() -> resilientService.callExternalService(), executor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            var maxConcurrency = futures.stream()
                    .mapToLong(f -> f.join().concurrentCount())
                    .max().orElse(-1);

            var maxId = futures.stream()
                    .mapToLong(f -> f.resultNow().callCount())
                    .max().orElse(-1);

            assertTrue(resilientService.getExceptionCounter().get() > 0);
            assertEquals(calls - 1, maxId);
            assertEquals(5, maxConcurrency);
        }
    }



    @Test
    @Disabled
    void test_getStateOrProvinceAndCountryNames_shouldThrowCallNotPermittedException_whenCircuitBreakerIsOpen()
            throws Throwable {
        circuitBreakerRegistry.circuitBreaker("resilientExternalService").transitionToOpenState();
//        List<Long> stateOrProvinceIds = List.of(1L);
//        assertThrows(CallNotPermittedException.class,
//                () -> locationService.getStateOrProvinceAndCountryNames(stateOrProvinceIds));
//        verify(locationService, atLeastOnce()).handleLocationNameListFallback(any());
    }
}