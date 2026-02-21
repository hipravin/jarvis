package hipravin.jarvis.statistic;

import hipravin.jarvis.event.SearchCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class StatisticService {
    private final Logger log = LoggerFactory.getLogger(StatisticService.class);

    private final KafkaTemplate<String, SearchCompletedEvent> kafkaTemplate;

    public StatisticService(KafkaTemplate<String, SearchCompletedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    @EventListener(value = SearchCompletedEvent.class, condition = "@environment.getProperty('statistic.search.enabled') == 'true'")
    public void searchCompleted(SearchCompletedEvent searchCompletedEvent) {
        kafkaTemplate.send("search-stat-topic", searchCompletedEvent)
                .whenComplete((sr, e) -> {
                    if (e != null) {
                        log.error(e.getMessage(), e);
                    }
                });

        log.info("Search completed: {} matches, took: {} ms, request: '{}'",
                searchCompletedEvent.getResponse().responseItems().size(),
                searchCompletedEvent.getElapsed().toMillis(),
                searchCompletedEvent.getRequest().query());
    }
}