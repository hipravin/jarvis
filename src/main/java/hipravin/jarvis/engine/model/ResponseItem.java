package hipravin.jarvis.engine.model;

public record ResponseItem(
        Link header,
        SearchProviderType searchProvider,
        String shortDescription
) {
}
