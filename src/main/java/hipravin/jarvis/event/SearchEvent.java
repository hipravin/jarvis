package hipravin.jarvis.event;

import org.springframework.context.ApplicationEvent;

public abstract class SearchEvent extends ApplicationEvent {
    private final String query;

    public SearchEvent(Object source, String query) {
        super(source);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
