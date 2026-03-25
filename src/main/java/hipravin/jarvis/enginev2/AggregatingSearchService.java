package hipravin.jarvis.enginev2;

import hipravin.jarvis.engine.model.InformationSource;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.*;

import static java.util.concurrent.CompletableFuture.supplyAsync;

@Service
@Primary
public class AggregatingSearchService implements SearchService {
    private static final Logger log = LoggerFactory.getLogger(AggregatingSearchService.class);

    private final long timeoutMs;

    private final List<SearchService> services;

    public AggregatingSearchService(
            @NotNull @Value("${jarvis.search-service.timeout-ms}") Long timeoutMs,
            List<SearchService> services) {
        this.timeoutMs = timeoutMs;
        this.services = services;
    }

    record ServiceQueryTask(SearchService service, CompletableFuture<SearchResponse> responseFuture) {
    }

    @Override
    public SearchResponse search(SearchRequest request) {
        final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

        List<ServiceQueryTask> tasks = services.stream()
                .filter(service -> request.informationSources().contains(service.getSource()))
                .map(service -> new ServiceQueryTask(service,
                        supplyAsync(() -> service.search(request), executor)
                                .exceptionally(SearchResponse::failed)))
                .toList();

        awaitCompletionAndLogTimedOut(executor, tasks, request);

        SearchResponse[] responses = tasks.stream()
                .map(task -> (task.responseFuture().isDone())
                        ? task.responseFuture().resultNow()
                        : SearchResponse.failed(task.service().getSource().alias() + ": request timed out"))
                .toArray(SearchResponse[]::new);

        return SearchResponse.join(responses);
    }

    private void awaitCompletionAndLogTimedOut(ExecutorService executor,
                                               List<ServiceQueryTask> tasks,
                                               SearchRequest request) {
        try {
            executor.shutdown();
            boolean completed = executor.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS);
            if (!completed) {
                tasks.forEach(task -> {
                    if (!task.responseFuture().isDone()) {
                        log.warn("Search request has timed out for service '{}', query: '{}'",
                                task.service().getSource(), request);
                    }
                });
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public InformationSource getSource() {
        throw new UnsupportedOperationException();
    }
}
