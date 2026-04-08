package hipravin.jarvis.statistic;

import hipravin.jarvis.event.SearchCompletedEvent;
import hipravin.jarvis.statistic.dto.SearchCompletedDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class StatisticGathererService {
    private final Logger log = LoggerFactory.getLogger(StatisticGathererService.class);

    private final KafkaTemplate<String, SearchCompletedDto> kafkaTemplate;

    public StatisticGathererService(KafkaTemplate<String, SearchCompletedDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Async
    @EventListener(value = SearchCompletedEvent.class, condition = "@environment.getProperty('statistic.search.enabled') == 'true'")
    public void onSearchCompleted(SearchCompletedEvent searchCompletedEvent) {
        kafkaTemplate.send("search-stat-topic", SearchCompletedDto.fromEvent(searchCompletedEvent))
                .whenComplete((sr, e) -> {
                    log.debug("Message has sent to kafka: {}", sr);
                    if (e != null) {
                        log.error(e.getMessage(), e);
                    }
                });

        log.debug("Search completed: {} matches, took: {} ms, request: '{}'",
                searchCompletedEvent.getResponse().excerpts().size(),
                searchCompletedEvent.getElapsed().toMillis(),
                searchCompletedEvent.getRequest().query());
    }
}