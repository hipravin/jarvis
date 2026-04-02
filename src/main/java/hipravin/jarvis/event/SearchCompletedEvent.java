package hipravin.jarvis.event;

import hipravin.jarvis.enginev2.dto.SearchRequest;
import hipravin.jarvis.enginev2.dto.SearchResponse;
import org.springframework.context.ApplicationEvent;

import java.io.Serial;
import java.time.Duration;

public class SearchCompletedEvent extends ApplicationEvent {
    @Serial
    private static final long serialVersionUID = 4035962450760777706L;

    private final transient SearchRequest request;
    private final transient SearchResponse response;
    private final Duration elapsed;

    public SearchCompletedEvent(Object source, SearchRequest request, SearchResponse response, Duration elapsed) {
        super(source);
        this.request = request;
        this.response = response;
        this.elapsed = elapsed;
    }

    public SearchRequest getRequest() {
        return request;
    }

    public SearchResponse getResponse() {
        return response;
    }

    public Duration getElapsed() {
        return elapsed;
    }
}
