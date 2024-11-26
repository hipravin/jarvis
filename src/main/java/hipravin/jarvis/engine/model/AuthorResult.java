package hipravin.jarvis.engine.model;

public record AuthorResult(
        String author,
        int count
) {
    public static final String TOTAL = "Total";
    public static final String OTHERS = "others";
}
