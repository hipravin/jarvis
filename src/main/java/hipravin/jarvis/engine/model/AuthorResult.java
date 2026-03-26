package hipravin.jarvis.engine.model;

public record AuthorResult(
        String author,
        long count
) {
    public static final String OTHERS = "";
}
