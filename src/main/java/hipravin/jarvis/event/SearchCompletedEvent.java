package hipravin.jarvis.event;

import hipravin.jarvis.engine.model.JarvisRequest;
import hipravin.jarvis.engine.model.JarvisResponse;
import org.springframework.context.ApplicationEvent;

import java.time.Duration;

public class SearchCompletedEvent extends ApplicationEvent {
    private final JarvisRequest request;
    private final JarvisResponse response;
    private final Duration elapsed;



    public SearchCompletedEvent(Object source, JarvisRequest request, JarvisResponse response, Duration elapsed) {
        super(source);
        this.request = request;
        this.response = response;
        this.elapsed = elapsed;
    }

    public static SearchCompletedEvent of(JarvisRequest request, JarvisResponse response, Duration elapsed, Object source) {
        return new SearchCompletedEvent(source, request, response, elapsed);
    }

    public JarvisRequest getRequest() {
        return request;
    }

    public JarvisResponse getResponse() {
        return response;
    }

    public Duration getElapsed() {
        return elapsed;
    }
}
