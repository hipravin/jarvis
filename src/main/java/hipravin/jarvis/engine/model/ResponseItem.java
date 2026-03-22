package hipravin.jarvis.engine.model;

public record ResponseItem(
        Link header,
        InformationSource searchProvider,
        String shortDescription
) {
}
