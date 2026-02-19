package hipravin.jarvis.statistic;

import hipravin.jarvis.event.SearchCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class StatisticService {
    private final Logger log = LoggerFactory.getLogger(StatisticService.class);

    @Async
    @EventListener(value = SearchCompletedEvent.class, condition = "@environment.getProperty('statistic.search.enabled') == 'true'")
    public void searchCompleted(SearchCompletedEvent search) {
        log.info("Search completed: {} matches, took: {} ms, request: '{}'",
                search.getResponse().responseItems().size(),
                search.getElapsed().toMillis(),
                search.getRequest().query());
    }
}