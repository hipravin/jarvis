package hipravin.jarvis.statistic.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import hipravin.jarvis.event.SearchCompletedEvent;

import java.time.Duration;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SearchCompletedDto(
        SearchRequest request,
        SearchResponse response,
        Duration elapsed
) {
    public static SearchCompletedDto fromEvent(SearchCompletedEvent event) {
        return new SearchCompletedDto(event.getRequest(), event.getResponse(), event.getElapsed());
    }
}
